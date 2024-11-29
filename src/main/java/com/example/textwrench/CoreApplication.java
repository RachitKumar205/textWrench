package com.example.textwrench;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import java.io.IOException;
import java.util.Objects;

public class CoreApplication extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("text-editor.fxml"));
            Parent mainContent = loader.load();

            // Create custom title bar
            HBox titleBar = createCustomTitleBar(primaryStage);

            // Create main layout
            VBox root = new VBox(titleBar, mainContent);

            root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

            VBox.setVgrow(mainContent, Priority.ALWAYS);

            // Create the scene with a modern dark theme
            Scene scene = new Scene(root, 1200, 800);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

            // Configure the stage
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setScene(scene);

            // Make the window draggable
            makeStageDraggable(primaryStage, titleBar);

            // Set initial window state
            setInitialWindowState(primaryStage);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HBox createCustomTitleBar(Stage stage) {
        HBox titleBar = new HBox(10);  // 10 is spacing between elements
        titleBar.setId("titleBar");
        titleBar.setAlignment(Pos.CENTER_LEFT);  // Vertically center items

        // Icon
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("app-icon.png")));
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(20);
        iconView.setFitHeight(20);

        // App name label
        Label appNameLabel = new Label("TextWrench");
        appNameLabel.getStyleClass().add("titlebar-label");  // CSS class for styling
        appNameLabel.getStyleClass().add("app-title");  // CSS class for styling

        // Optional menu buttons (example)
        //HBox menuButtons = new HBox(5);
        //Button fileButton = new Button("File");
        //Button editButton = new Button("Edit");
        //menuButtons.getChildren().addAll(fileButton, editButton);

        // Spacer to push window controls right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Window control buttons
        HBox windowControls = createWindowControlButtons(stage);

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            titleBar.getChildren().addAll(
                    windowControls,
                    appNameLabel,
                    spacer
            );
        } else {
            titleBar.getChildren().addAll(
                    appNameLabel,
                    spacer,
                    windowControls
            );
        }

        return titleBar;
    }

    private HBox createWindowControlButtons(Stage stage) {
        HBox controls = new HBox(1);  // Reduced spacing to 3
        controls.setSpacing(1);

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            controls.setAlignment(Pos.CENTER_LEFT);
            controls.getChildren().addAll(
                    createMacOSStyleButton(FontIcon.of(BootstrapIcons.CIRCLE_FILL, 12, Color.web("#fa5e57")), () -> Platform.exit()),
                    createMacOSStyleButton(FontIcon.of(BootstrapIcons.CIRCLE_FILL, 12, Color.web("#fabc2f")), () -> stage.setIconified(true)),
                    createMacOSStyleButton(FontIcon.of(BootstrapIcons.CIRCLE_FILL, 12, Color.web("#28c840")), () -> {
                        stage.setFullScreen(!stage.isFullScreen());
                    })
            );
        } else {
            controls.setAlignment(Pos.CENTER_RIGHT);
            controls.getChildren().addAll(
                    createWindowsStyleButton(FontIcon.of(Material.MINIMIZE, 16), () -> stage.setIconified(true)),
                    createWindowsStyleButton(FontIcon.of(Material.FULLSCREEN, 16), () -> {
                        stage.setMaximized(!stage.isMaximized());
                        isMaximized = stage.isMaximized();
                    }),
                    createWindowsStyleButton(FontIcon.of(Material.CLOSE, 16), () -> Platform.exit())
            );
        }

        return controls;
    }

    private Button createMacOSStyleButton(FontIcon icon, Runnable action) {
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;" +  // Remove any internal padding
                        "-fx-min-width: 20px;" +
                        "-fx-min-height: 20px;"
        );
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createWindowsStyleButton(FontIcon icon, Runnable action) {
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 20px;" +
                        "-fx-hover-color: rgba(255, 255, 255, 0.2);"
        );
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createWindowButton(String text, String style, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle(style + " -fx-text-fill: black; -fx-min-width: 30px; -fx-min-height: 20px;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void makeStageDraggable(Stage stage, HBox titleBar) {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void setInitialWindowState(Stage primaryStage) {
        // Set the window to full width and height on both macOS and Windows
        var bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        // Optional: Make the window resizable if needed
        primaryStage.setResizable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}