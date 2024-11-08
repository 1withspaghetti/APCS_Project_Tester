package me.the1withspaghetti.tests;

public class NullValueTestException extends TestException {
    public NullValueTestException() {
        this("Unexpected null value");
    }

    public NullValueTestException(String msg) {
        super(msg);
    }
}
