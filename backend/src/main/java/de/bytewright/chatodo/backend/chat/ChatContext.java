package de.bytewright.chatodo.backend.chat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChatContext {
    protected ChatSender mainChatSender;
    protected Long channelAddTodos;
    protected Long channelShopping;
    protected Long channelShowTodos;


    protected void initContext(Function<ChatContext, ChatSender> mainChannelSender) {
        mainChatSender = mainChannelSender.apply(this);
    }

    public Optional<Long> getChannelAddTodos() {
        return Optional.ofNullable(channelAddTodos);
    }

    public Optional<Long> getChannelShopping() {
        return Optional.ofNullable(channelShopping);
    }

    public Optional<Long> getChannelShowTodos() {
        return Optional.ofNullable(channelShowTodos);
    }

    public boolean isRegistrationComplete() {
        return channelAddTodos != null &&
                channelShopping != null &&
                channelShowTodos != null;
    }

    public ChatSender getMainChannelSender() {
        return mainChatSender;
    }
}
