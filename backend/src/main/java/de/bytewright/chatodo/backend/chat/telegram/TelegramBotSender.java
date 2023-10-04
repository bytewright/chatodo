package de.bytewright.chatodo.backend.chat.telegram;

import de.bytewright.chatodo.backend.chat.ChatContext;
import de.bytewright.chatodo.backend.chat.ChatSender;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

public class TelegramBotSender implements ChatSender {

    private TelegramChatContext chatContext;


    public TelegramBotSender(ChatContext chatContext) {
        if (!(chatContext instanceof TelegramChatContext)) {
            throw new IllegalStateException("TelegramSender without telegramchat! " + chatContext);
        }
        this.chatContext = (TelegramChatContext) chatContext;
    }

    @Override
    public void accept(String s) {

    }
}
