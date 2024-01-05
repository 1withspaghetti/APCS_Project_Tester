package me.the1withspaghetti.tests;

public class Assertions {

    public static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual);
    }

    public static void assetEquals(String expected, String actual, String msg) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual, msg);
    }
}
