package de.bytewright.chatodo.backend.chat.nlp;

public class MsgClassification {
    private final String inputText;
    private String intentClassifiction;

    public MsgClassification(String inputText) {
        this.inputText = inputText;
    }

    public String getInputText() {
        return inputText;
    }

    public String getType() {
        return null;
    }

    public String getIntentClassification() {
        return intentClassifiction;
    }

    public void setIntentClassifiction(String intentClassifiction) {
        this.intentClassifiction = intentClassifiction;
    }
}
