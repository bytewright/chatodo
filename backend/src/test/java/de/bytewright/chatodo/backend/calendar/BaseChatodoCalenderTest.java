package de.bytewright.chatodo.backend.calendar;

import de.bytewright.chatodo.util.ConnectionSettings;

public class BaseChatodoCalenderTest {
    /**
     * ToDo: Add your settings here for the tests to work
     */
    protected ConnectionSettings getConSettings() {
        return new ConnectionSettings("", "usr", "pw");
    }
}
