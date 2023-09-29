package de.bytewright.chatodo.backend.chat;

import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;

public class MsgResponder {
    public int canAnswer(String classificationType) {
        return 0;
    }

    public ChatResponse generateResponse(MsgClassification messageClassification) {
        return new ChatResponse();
    }
}
