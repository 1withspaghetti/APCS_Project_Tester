package me.the1withspaghetti;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import me.the1withspaghetti.events.TestStartEvent;
import me.the1withspaghetti.panals.ControlPanel;
import me.the1withspaghetti.panals.MainPanel;
import me.the1withspaghetti.tests.AbstractTest;
import me.the1withspaghetti.tests.impl.PrioritizingPatientsTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class App extends Application {

    public static final List<AbstractTest> TESTS = List.of(new PrioritizingPatientsTest());

    @Override
    public void start(Stage stage) {

        Label title = new Label("APCS Project Tester by Tyler Place");
        title.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));

        ControlPanel controlPanel = new ControlPanel(stage);

        MainPanel mainPanel = new MainPanel();

        VBox vbox = new VBox(
                title,
                controlPanel.get(),
                new Separator(),
                mainPanel.get());
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(10, 10, 0, 10));
        vbox.setSpacing(10);

        vbox.setMaxHeight(Double.MAX_VALUE);

        controlPanel.get().addEventHandler(TestStartEvent.TEST_START, e->{
            System.out.println("Running test: "+e.getTest().getTestName());
            mainPanel.runTest(e.getTest(), e.getFile());
        });

        String version = getClass().getPackage().getSpecificationVersion();
        System.out.println("Program version: "+version);

        Scene scene = new Scene(vbox, 750, 500);
        stage.setTitle("APCS Project Tester by Tyler Place"+(version != null ? " "+version : ""));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}