package me.the1withspaghetti.panals;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import me.the1withspaghetti.tests.AbstractTest;

import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainPanel {

    private VBox root;
    //private HashMap<AbstractTest, TestPanel> cache = new HashMap<AbstractTest, TestPanel>();

    public MainPanel() {
        root = new VBox(new Label("Select a project above and click \"Run Tests\""));
        //root.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        root.setAlignment(Pos.TOP_CENTER);
    }
    public Node get() {
        return root;
    }

    private TestPanel last = null;

    public void runTest(AbstractTest test, File code) {
        if (last != null) {
            last.close();
        }
        root.getChildren().remove(0, root.getChildren().size());

        TestPanel panel = new TestPanel();
        root.getChildren().add(panel.get());
        last = panel;
        test.run(panel, code);
    }
}
