package de.bytewright.chatodo.backend.chat;

import org.springframework.stereotype.Service;

@Service
public class ChannelChatService {
    public void processMessage(ChatContext chatContext, String text) {
        if (!chatContext.isRegistrationComplete()) {
            doChannelRegistration(chatContext, text);
        }
    }

    private void doChannelRegistration(de.bytewright.chatodo.backend.chat.ChatContext chatContext, String text) {
        chatContext.getMainChannelSender();
    }
}
