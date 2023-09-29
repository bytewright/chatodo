package de.bytewright.chatodo.backend.chat;

import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;

public class NoAnswerResponder extends MsgResponder {
    @Override
    public int canAnswer(String classificationType) {
        return Integer.MIN_VALUE;
    }

    @Override
    public ChatResponse generateResponse(MsgClassification messageClassification) {
        String intentClassification = messageClassification.getIntentClassification();
        return new ChatResponse("Thanks for the message! Unfortunately I can't answer that. Was your intent: " + intentClassification);
    }
}
