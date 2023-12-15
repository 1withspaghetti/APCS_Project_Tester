package me.the1withspaghetti.panals;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.StringConverter;
import me.the1withspaghetti.App;
import me.the1withspaghetti.events.TestStartEvent;
import me.the1withspaghetti.tests.AbstractTest;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class ControlPanel {

    private Node root;

    public ControlPanel(Stage stage) {

        AtomicReference<File> current = new AtomicReference<>(null);

        Label fileLabel = new Label("Select Java File:");
        FileChooser fileChooser = new FileChooser();
        Button chooseButton = new Button("Browse...");
        Label fileName = new Label("No file selected");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Source File", "*.java"));
        try{
            fileChooser.setInitialDirectory(new File(ControlPanel.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        chooseButton.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                fileName.setText(file.getName());
                current.set(file);
            }
        });

        HBox hbox1 = new HBox(fileLabel, chooseButton, fileName);
        hbox1.setSpacing( 10.0d );
        hbox1.setAlignment(Pos.CENTER );



        Label selectLabel = new Label("Select Project:");
        ChoiceBox<Pair<String,String>> choiceBox = new ChoiceBox<>();
        choiceBox.setConverter( new StringConverter<Pair<String,String>>() {
            @Override
            public String toString(Pair<String, String> pair) {
                return pair.getKey();
            }
            @Override
            public Pair<String, String> fromString(String string) {
                return null;
            }
        });
        for (AbstractTest test : App.TESTS) choiceBox.getItems().add(new Pair<>(test.getTestName(), test.getTestName()));
        choiceBox.setValue(choiceBox.getItems().get(0));
        choiceBox.setPrefWidth(200);

        HBox hbox2 = new HBox(
                selectLabel,
                choiceBox);
        hbox2.setSpacing( 10.0d );
        hbox2.setAlignment(Pos.CENTER);


        Button testButton = new Button("Run Tests");
        testButton.setOnAction(actionEvent -> {
            for (AbstractTest test : App.TESTS) {
                if (test.getTestName().equals(choiceBox.getValue().getKey()) && current.get() != null) {
                    testButton.fireEvent(new TestStartEvent(TestStartEvent.TEST_START, current.get(), test));
                }
            }
        });

        VBox vbox = new VBox(hbox1, hbox2, testButton);
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER);

        this.root = vbox;
    }

    public Node get() {
        return root;
    }
}
