package me.the1withspaghetti.tests;

public class NotEqualTestException extends TestException {
    public NotEqualTestException(Object expected, Object actual) {
        this(expected, actual, "Unexpected value in output");
    }

    public NotEqualTestException(Object expected, Object actual, String msg) {
        super(msg+"\nExpected : "+expected+"\nActual   : "+actual);
    }
}
