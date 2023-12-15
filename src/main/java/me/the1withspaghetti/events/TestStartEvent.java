package me.the1withspaghetti.events;

import javafx.event.Event;
import javafx.event.EventType;
import me.the1withspaghetti.tests.AbstractTest;

import java.io.File;

public class TestStartEvent extends Event {

    public static final EventType<TestStartEvent> TEST_START = new EventType<>(ANY, "TEST_START");

    private File file;
    private AbstractTest test;
    public TestStartEvent(EventType<? extends Event> eventType, File file, AbstractTest test) {
        super(eventType);
        this.file = file;
        this.test = test;
    }

    public File getFile() {
        return file;
    }

    public AbstractTest getTest() {
        return test;
    }
}
