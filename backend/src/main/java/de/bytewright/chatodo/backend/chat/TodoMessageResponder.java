package de.bytewright.chatodo.backend.chat;

import de.bytewright.chatodo.backend.calendar.CalendarService;
import de.bytewright.chatodo.backend.chat.nlp.MsgClassification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TodoMessageResponder implements MessageResponderGenerator {
    private static final String TODO_PATTERN = "(todo|ToDo|Todo|Aufgabe)";
    private final CalendarService calendarService;

    @Autowired
    public TodoMessageResponder(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public int canAnswer(MsgClassification messageClassification) {
        if (Pattern.matches(TODO_PATTERN, messageClassification.getInputText())) {
            return 1000;
        }
        return 0;
    }

    @Override
    public ChatResponse generateResponse(MsgClassification messageClassification) {
calendarService.createTodo();
        return new ChatResponse("You want to create a ToDo Task!");
    }
}
