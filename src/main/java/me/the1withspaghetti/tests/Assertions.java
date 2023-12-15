package me.the1withspaghetti.tests;

public class Assertions {

    public static void assertEquals(String actual, String expected) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual);
    }

    public static void assetEquals(String actual, String expected, String msg) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual, msg);
    }
}
