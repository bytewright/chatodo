package de.bytewright.chatodo.backend.chat.telegram;

import de.bytewright.chatodo.backend.AppSecretsService;
import de.bytewright.chatodo.backend.chat.ChannelChatService;
import de.bytewright.chatodo.backend.chat.ChatContext;
import de.bytewright.chatodo.backend.chat.ChatResponse;
import de.bytewright.chatodo.backend.chat.MessageResponderGenerator;
import de.bytewright.chatodo.backend.chat.NoAnswerResponder;
import de.bytewright.chatodo.backend.chat.nlp.IntentRecognitionService;
import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBot extends TelegramLongPollingBot implements HealthIndicator, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final String TELEGRAM_BOT_TOKEN = "chat.telegrambot.token";
    private static final String TELEGRAM_BOT_USRNAME = "chat.telegrambot.username";
    private static final String TELEGRAM_BOT_REMOTE_ID = "chat.telegrambot.myname";

    private final AppSecretsService appSecretsService;
    private final IntentRecognitionService intentRecognition;
    private final List<MessageResponderGenerator> msgResponderList;
    private final ChannelChatService channelChatService;
    private BotSession botSession;
    private final Map<Long, ChatContext> channelMap = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBot(AppSecretsService appSecretsService, IntentRecognitionService intentRecognition, List<MessageResponderGenerator> msgResponderList, ChannelChatService channelChatService) {
        super(getToken(appSecretsService));
        this.appSecretsService = appSecretsService;
        this.intentRecognition = intentRecognition;
        this.msgResponderList = msgResponderList;
        this.channelChatService = channelChatService;
    }

    private static String getToken(AppSecretsService appSecretsService) {
        return appSecretsService.getString(TELEGRAM_BOT_TOKEN).orElseThrow(() -> new IllegalArgumentException("Failed to find telegram bot token"));
    }

    @Override
    public void onUpdateReceived(Update update) {
        Optional<ChatContext> optionalChatContext = getChannel(update);
        if (optionalChatContext.isPresent()) {
            processUpdate(optionalChatContext.get(), update);
            return;
        }

        Message message = update.getMessage();
        log.info("Got Telegram Update: {}", message);
        MsgClassification messageClassification = intentRecognition.classify(message.getText());
        MessageResponderGenerator msgResponder = findMsgResponder(messageClassification);
        ChatResponse chatResponse = msgResponder.generateResponse(messageClassification);
        SendMessage sm = SendMessage.builder()
                .chatId(message.getFrom().getId())
                .text(chatResponse.getText())
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            log.error("Exception while sending msg as response to: {}", update, e);
        }
    }

    private void processUpdate(ChatContext chatContext, Update update) {
        log.info("got update for known chatContext {}", chatContext);
        channelChatService.processMessage(chatContext, update.getMessage().getText());

    }

    private Optional<ChatContext> getChannel(Update update) {
        log.info("Trying to determine channel for update: {}", update);
        Long id = update.getChannelPost().getChat().getId();
        log.info("channelid={}?", id);
        ChatContext chatContext = channelMap.computeIfAbsent(id, ignore -> startNewContext(update));
        return Optional.ofNullable(chatContext);
    }

    private ChatContext startNewContext(Update update) {
        log.info("starting new context for update: {}", update);
        ChatContext chatContext = new TelegramChatContext(this, update);
        return null;
    }

    private MessageResponderGenerator findMsgResponder(MsgClassification msgClassification) {
        return msgResponderList.stream()
                .max(Comparator.comparing(msgResponder -> msgResponder.canAnswer(msgClassification)))
                .orElse(new NoAnswerResponder());
    }

    @Override
    public String getBotToken() {
        return getToken(appSecretsService);
    }

    @Override
    public String getBotUsername() {
        return appSecretsService.getString(TELEGRAM_BOT_USRNAME).orElseThrow(() -> new IllegalArgumentException("Failed to find telegram bot name"));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting bot service...");

        TelegramBotsApi botsApi = null;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            this.botSession = botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Health health() {
        if (!botSession.isRunning()) {
            return Health.down()
                    .withDetails(Map.of("telegram-botsession", "down"))
                    .build();
        }
        return Health.up().build();
    }
}
