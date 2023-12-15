package me.the1withspaghetti.tests;

public class NotEqualTestException extends TestException {
    public NotEqualTestException(String expected, String actual) {
        this(expected, actual, "Unexpected value in output");
    }

    public NotEqualTestException(String expected, String actual, String msg) {
        super(msg+"\nExpected : "+expected+"\nActual   : "+actual);
    }
}
