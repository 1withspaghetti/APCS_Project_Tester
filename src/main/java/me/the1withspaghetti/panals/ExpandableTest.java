package me.the1withspaghetti.panals;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.the1withspaghetti.tests.AbstractTestRunner;
import me.the1withspaghetti.tests.TestException;
import me.the1withspaghetti.util.ConsoleBuffer;
import me.the1withspaghetti.util.TeeOutputStream;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.concurrent.ExecutorService;

public class ExpandableTest implements Closeable {

    private final TitledPane root;
    private final TextFlow content;

    private final PrintStream out;
    private final PrintStream err;
    private final PrintStream inout;

    private final ExecutorService exe;

    public ExpandableTest(String testName, ExecutorService exe) {
        this.exe = exe;

        content = new TextFlow();
        content.setPadding(new Insets(3));

//        ScrollPane scrollPane = new ScrollPane(content);
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        root = new TitledPane(testName, content);
        root.setExpanded(false);
        root.setAnimated(true);
        root.setMaxWidth(Double.MAX_VALUE);

        out = new PrintStream(new OutputStream() {
            @Override
            public void write(int i) {
                writeText(i, Color.BLACK);
            }
        });
        err = new PrintStream(new OutputStream() {
            @Override
            public void write(int i) {
                writeText(i, Color.RED);
            }
        });
        inout = new PrintStream(new OutputStream() {
            @Override
            public void write(int i) {
                writeText(i, Color.BLUE);
            }
        });
    }

    private Color lastColor = Color.BLACK;
    private synchronized void writeText(int i, Color c) {
        Platform.runLater(()->{
            if (c.equals(lastColor) && !content.getChildren().isEmpty()) {
                Text current = (Text)content.getChildren().get(content.getChildren().size() - 1);
                current.setText(current.getText() + (char)i);
            } else {
                Text n = new Text(String.valueOf((char) i));
                n.setFont(Font.font("Consolas"));
                n.setFill(c);
                content.getChildren().add(n);
                lastColor = c;
            }
        });
    }

    public Node get() {
        return root;
    }

    private void setStatus(TestStatus status) {
        Platform.runLater(()->{
            switch (status){
                case BLANK:
                    setGraphic("");
                    break;
                case LOADING:
                    setGraphic("loading.gif");
                    break;
                case FAILED:
                    setGraphic("fail.png");
                    break;
                case SUCCESS:
                    setGraphic("success.png");
                    break;
            }
        });
    }

    public void executeTest(AbstractTestRunner test) {

        exe.submit(()->{
            try {
                setStatus(TestStatus.LOADING);

                PipedInputStream newIn = new PipedInputStream();
                PipedOutputStream simInStream = new PipedOutputStream(newIn);
                PrintStream simIn = new PrintStream(new TeeOutputStream(simInStream, inout));

                ConsoleBuffer simOut = new ConsoleBuffer(2048, out);
                PrintStream newOut = new PrintStream(simOut);

                InputStream oldIn = System.in;
                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;

                System.setIn(newIn);
                System.setOut(newOut);
                System.setErr(err);

                try {
                    test.run(out, err, simIn, simOut);
                    setStatus(TestStatus.SUCCESS);
                } catch (TestException e) {
                    err.println("\nError: "+e.getMessage());
                    //e.printStackTrace(err);
                    setStatus(TestStatus.FAILED);
                } catch (BufferUnderflowException e) {
                    err.println("\nError: Reached end of program while still expecting more input!");
                    setStatus(TestStatus.FAILED);
                } catch (BufferOverflowException e) {
                    err.println("\nError: Your program outputted wayyyy too much text, calm down!");
                    setStatus(TestStatus.FAILED);
                } catch (Exception e) {
                    err.println("\nUnexpected error while running test:");
                    e.printStackTrace(System.err);
                    setStatus(TestStatus.FAILED);
                }

                System.setIn(oldIn);
                System.setOut(oldOut);
                System.setErr(oldErr);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setGraphic(String file) {
        root.setGraphic(new ImageView(new Image(file, 24, 24, true, true)));
    }

    @Override
    public void close() {
        out.close();
        err.close();
        inout.close();
    }

    private enum TestStatus {
        BLANK,
        LOADING,
        SUCCESS,
        FAILED;
    }
}
