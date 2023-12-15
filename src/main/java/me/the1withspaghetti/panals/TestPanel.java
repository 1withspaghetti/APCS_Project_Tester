package me.the1withspaghetti.panals;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.Closeable;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TestPanel implements Closeable {

    private final Node root;
    private final VBox vbox;

    private ExecutorService exe;

    private HashSet<ExpandableTest> tests = new HashSet<ExpandableTest>();

    public TestPanel() {
        vbox = new VBox();
        vbox.setSpacing(15);
        vbox.setAlignment(Pos.TOP_CENTER);
        //vbox.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setMaxHeight(Double.MAX_VALUE);
        vbox.setPadding(new Insets(5));

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBorder(Border.EMPTY);
        root = scrollPane;

        exe = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setPriority(Thread.currentThread().getPriority() - 1);
            return t;
        });
    }

    public Node get() {
        return root;
    }

    public ExpandableTest getNewTest(String testName) {
        ExpandableTest test = new ExpandableTest(testName, exe);
        tests.add(test);
        vbox.getChildren().add(test.get());
        return test;
    }

    @Override
    public void close() {
        exe.shutdownNow();
        tests.forEach(ExpandableTest::close);
    }
}
