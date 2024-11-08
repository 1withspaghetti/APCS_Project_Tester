package me.the1withspaghetti.tests;

public class Assertions {

    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) throw new NotEqualTestException(expected, actual);
    }

    public static void assertEquals(double expected, double actual, double delta, String msg) {
        if (Math.abs(expected - actual) > delta) throw new NotEqualTestException(expected, actual, msg);
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual);
    }

    public static void assertEquals(Object expected, Object actual, String msg) {
        if (!expected.equals(actual)) throw new NotEqualTestException(expected, actual, msg);
    }

    public static void assertTrue(boolean value) {
        assertEquals(true, value);
    }

    public static void assertTrue(boolean value, String msg) {
        assertEquals(true, value, msg);
    }

    public static void assertNotNull(Object value) {
        if (value == null) throw new NullValueTestException();
    }

    public static void assertNotNull(Object value, String msg) {
        if (value == null) throw new NullValueTestException(msg);
    }
}
