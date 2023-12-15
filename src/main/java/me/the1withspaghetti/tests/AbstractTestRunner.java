package me.the1withspaghetti.tests;

import me.the1withspaghetti.util.ConsoleBuffer;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Reader;

@FunctionalInterface
public interface AbstractTestRunner {
    void run(PrintStream out, PrintStream err, PrintStream in, ConsoleBuffer console) throws Exception;
}
