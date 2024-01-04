package me.the1withspaghetti.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

public class ConsoleReader extends BufferedReader {

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public ConsoleReader(Reader reader) {
        super(reader);
    }

    /**
     * Reads a line until it reaches a new line character or there are no more characters available, waiting for it to timeout
     * @return The string, or null if no characters are available
     */
    public String readLine(long timeout, TimeUnit unit) throws IOException, InterruptedException {
        StringBuilder str = new StringBuilder();
        long end = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < end &&
                !(str.length() >= LINE_SEPARATOR.length() &&
                  str.substring(str.length()-LINE_SEPARATOR.length()).equals(LINE_SEPARATOR))
        ) {
            if (ready()) {
                str.append((char) read());
            } else Thread.sleep(10);
        }
        if (str.length() >= LINE_SEPARATOR.length() &&
                str.substring(str.length()-LINE_SEPARATOR.length()).equals(LINE_SEPARATOR)) {
            return str.substring(0, str.length()-LINE_SEPARATOR.length());
        }
        return str.toString();
    }
}
