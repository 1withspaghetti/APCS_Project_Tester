package me.the1withspaghetti.util;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;

public class ConsoleBuffer extends OutputStream {

    final byte[] buf;
    final PrintStream out;

    int readPos = 0;
    int writePos = 0;
    public ConsoleBuffer(int size, PrintStream out) {
        buf = new byte[size];
        this.out = out;
    }

    public void write(int c) {
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

    // Waits up to 5000ms for a character and returns -1 if one does not appear
    public int readBlocking() throws InterruptedException {
        synchronized (buf) {
            if (readPos >= writePos) {
                System.err.println("Waiting on output");
                buf.wait(5000);
                if (hasRemaining()) return read();
                else return -1;
            }
            return read();
        }
    }

    // Waits 5000ms for the initial char and then 100ms for all previous
    public String nextLine() throws InterruptedException {
        synchronized (buf) {
            if (readPos >= writePos) {
                buf.wait(5000);
                if (!hasRemaining()) throw new BufferUnderflowException();
            }
            int start = readPos;
            int b = read();
            while (b != '\n') {
                if (readPos >= writePos) {
                    buf.wait(100);
                    if (hasRemaining()) b = read();
                    else return new String(Arrays.copyOfRange(buf, start, readPos));
                } else {
                    b = read();
                }
            }
            return new String(Arrays.copyOfRange(buf, start, readPos-2));
        }
    }

    public boolean hasRemaining() {
        return (readPos < writePos);
    }
}
