package de.bytewright.chatodo.backend.chat.telegram;

import de.bytewright.chatodo.backend.chat.ChatContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramChatContext extends ChatContext {
    private static final Logger log = LoggerFactory.getLogger(TelegramChatContext.class);
    private final TelegramBot telegramBot;
    private final Update update;
    private final Long mainChatId;


    public TelegramChatContext(TelegramBot telegramBot, Update update) {
        this.telegramBot = telegramBot;
        this.update = update;
        mainChatId = update.getMessage().getChatId();
        initContext(TelegramBotSender::new);
    }

    public Long getMainChat() {
        return mainChatId;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
}
