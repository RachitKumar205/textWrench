package com.example.textwrench.plugins;

import com.example.textwrench.coremodules.plugin.PluginContext;
import com.example.textwrench.coremodules.plugin.TextWrenchPlugin;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SketchPadPlugin implements TextWrenchPlugin {
    private boolean enabled = true;
    private PluginContext context;
    private Canvas canvas;
    private double lastX, lastY;

    public SketchPadPlugin() {}

    @Override
    public String getPluginId() {
        return "sketchpad.textwrenchplugin";
    }

    @Override
    public String getPluginName() {
        return "Sketch Pad Plugin";
    }

    @Override
    public String getPluginDescription() {
        return "TextWrench plugin for sketching and drawing, with support for saving and exporting images as bitmap";
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        this.canvas = new Canvas(800, 600); // Set canvas size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Add mouse event handlers for drawing
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);

        MenuItem openSketchPadMenuItem = new MenuItem("New Sketch");
        openSketchPadMenuItem.setOnAction(e -> openSketchPadInNewTab());
        context.addMenuItem("Sketchpad", openSketchPadMenuItem);


        MenuItem saveMenuItem = new MenuItem("Save Sketch");
        saveMenuItem.setOnAction(e -> saveSketch());
        context.addMenuItem("Sketchpad", saveMenuItem);

        MenuItem loadMenuItem = new MenuItem("Load Sketch");
        loadMenuItem.setOnAction(e -> loadSketch());
        context.addMenuItem("Sketchpad", loadMenuItem);
    }

    private void onMousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
    }

    private void onMouseDragged(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(lastX, lastY, x, y);
        lastX = x;
        lastY = y;
    }

    private void newSketch() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void saveSketch() {
        // Implement save logic
    }

    private void loadSketch() {
        // Implement load logic
    }

    private void openSketchPadInNewTab() {
        // Create a new Canvas
        Canvas newCanvas = new Canvas(800, 600);
        GraphicsContext gc = newCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

        // Add mouse event handlers for drawing
        newCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            lastX = event.getX();
            lastY = event.getY();
        });
        newCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double x = event.getX();
            double y = event.getY();
            GraphicsContext canvasGc = newCanvas.getGraphicsContext2D();
            canvasGc.setStroke(Color.BLACK);
            canvasGc.setLineWidth(2);
            canvasGc.strokeLine(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        });

        // Create a ScrollPane for the canvas
        ScrollPane scrollPane = new ScrollPane(newCanvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create a new tab
        Tab newTab = new Tab("Sketch Pad");
        newTab.setContent(scrollPane);

        // Add the new tab
        context.addTab(newTab);
    }

    @Override
    public Node getConfigurationPane() {
        VBox configPane = new VBox(10);
        configPane.getChildren().add(canvas);
        return configPane;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}