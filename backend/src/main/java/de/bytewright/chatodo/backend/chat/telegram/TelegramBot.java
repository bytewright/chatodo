package de.bytewright.chatodo.backend.chat.telegram;

import de.bytewright.chatodo.backend.AppSecretsService;
import de.bytewright.chatodo.backend.chat.ChatResponse;
import de.bytewright.chatodo.backend.chat.MsgResponder;
import de.bytewright.chatodo.backend.chat.NoAnswerResponder;
import de.bytewright.chatodo.backend.chat.nlp.IntentRecognitionService;
import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class TelegramBot extends TelegramLongPollingBot implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final String TELEGRAM_BOT_TOKEN = "chat.telegrambot.token";
    private static final String TELEGRAM_BOT_USRNAME = "chat.telegrambot.username";
    private static final String TELEGRAM_BOT_REMOTE_ID = "chat.telegrambot.myname";

    private final AppSecretsService appSecretsService;
    private final IntentRecognitionService intentRecognition;
    private final List<MsgResponder> msgResponderList;

    @Autowired
    public TelegramBot(AppSecretsService appSecretsService, IntentRecognitionService intentRecognition, List<MsgResponder> msgResponderList) {
        super(getToken(appSecretsService));
        this.appSecretsService = appSecretsService;
        this.intentRecognition = intentRecognition;
        this.msgResponderList = msgResponderList;
    }

    private static String getToken(AppSecretsService appSecretsService) {
        return appSecretsService.getString(TELEGRAM_BOT_TOKEN).orElseThrow(() -> new IllegalArgumentException("Failed to find telegram bot token"));
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        log.info("Got Telegram Update: {}", message);
        MsgClassification messageClassification = intentRecognition.classify(message.getText());
        MsgResponder msgResponder = findMsgResponder(messageClassification);
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

    private MsgResponder findMsgResponder(MsgClassification msgClassification) {
        return msgResponderList.stream()
                .max(Comparator.comparing(msgResponder -> msgResponder.canAnswer(msgClassification.getType())))
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
            BotSession botSession = botsApi.registerBot(this);
            // todo add BotSession to Health
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
