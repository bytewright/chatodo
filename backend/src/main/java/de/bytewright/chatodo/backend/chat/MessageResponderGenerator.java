package de.bytewright.chatodo.backend.chat;

import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;

public interface MessageResponderGenerator {
    int canAnswer(MsgClassification messageClassification);

    ChatResponse generateResponse(MsgClassification messageClassification);
}
