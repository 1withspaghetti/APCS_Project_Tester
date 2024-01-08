package me.the1withspaghetti.util;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ConsoleReader implements Closeable {

    private final byte[] buf;
    private final PrintStream out;

    private int readPos = 0;
    private int writePos = 0;

    private final InputStream in;
    private final Thread readThread;

    private boolean readClosed = false;

    public ConsoleReader(InputStream in, PrintStream out, int bufferSize) {
        buf = new byte[bufferSize];
        this.out = out;
        this.in = in;
        this.readThread = new Thread(()->{
            try {
                int i = in.read();
                while (i != -1 && !readClosed) {
                    write(i);
                    i = in.read();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            readClosed = true;
        });
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    private void write(int c) {
        if (c == '\r') return;
        synchronized (buf) {
            if (writePos >= buf.length) throw new BufferOverflowException();
            buf[writePos] = (byte) c;
            writePos++;
            buf.notifyAll();
        }
    }

    public int read() {
        if (readPos >= writePos) throw new BufferUnderflowException();
        int c = buf[readPos] & 0xff;
        readPos++;
        out.print((char)c);
        return c;
    }

    /**
     * Reads a line until it reaches a new line character or there are no more characters available, waiting for it to timeout
     * @return The string, or null if no characters are available
     */
    public String nextLine(long timeout) throws InterruptedException {
        synchronized (buf) {
            if (!hasRemaining()) {
                if (!readClosed) buf.wait(timeout);
                if (!hasRemaining()) return "";
            }
            StringBuilder str = new StringBuilder();
            str.append((char)read());
            while (str.charAt(str.length()-1) != '\n') {
                if (!hasRemaining()) {
                    if (!readClosed) buf.wait(50);
                    if (hasRemaining()) str.append((char)read());
                    else break;
                } else {
                    str.append((char)read());
                }
            }
            return str.substring(0, str.length() - ((str.length() >= 1 && str.charAt(str.length()-1) == '\n') ? 1 : 0));
        }
    }

    public boolean hasRemaining() {
        return (readPos < writePos);
    }

    @Override
    public void close() throws IOException {
        readClosed = true;
        in.close();
        readThread.interrupt();
    }
}
