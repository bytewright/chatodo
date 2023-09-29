package de.bytewright.chatodo.backend.chat;

public class ChatResponse {
    private final String text;

    public ChatResponse() {
        this.text = null;
    }

    public ChatResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
