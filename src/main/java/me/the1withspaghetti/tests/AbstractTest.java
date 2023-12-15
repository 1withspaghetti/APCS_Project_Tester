package me.the1withspaghetti.tests;

import me.the1withspaghetti.panals.TestPanel;

import java.io.File;

public interface AbstractTest {
    String getTestName();

    public void run(TestPanel panel, File code);
}
