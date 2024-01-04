package me.the1withspaghetti.tests;

import java.io.PrintStream;

@FunctionalInterface
public interface AbstractTestRunner {
    void run(PrintStream out, PrintStream inout, PrintStream err) throws Exception;
}
