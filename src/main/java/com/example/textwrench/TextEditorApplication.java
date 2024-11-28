package com.example.textwrench;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class TextEditorApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("text-editor.fxml"));
            Parent root = loader.load();

            // Create the scene with a modern dark theme
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

            primaryStage.setTitle("textWrench");
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);

            // Set the window to full width and height on both macOS and Windows
            var bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            // Optional: Make the window resizable if needed
            primaryStage.setResizable(true);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
