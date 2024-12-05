package com.example.textwrench.plugins;

import com.example.textwrench.coremodules.plugin.PluginContext;
import com.example.textwrench.coremodules.plugin.TextWrenchPlugin;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class SketchPadPlugin implements TextWrenchPlugin {
    private boolean enabled = true;
    private PluginContext context;
    private double lastX, lastY;

    // Shape drawing modes
    private enum DrawMode {
        FREEHAND, RECTANGLE, CIRCLE, TRIANGLE, LINE
    }
    private DrawMode currentMode = DrawMode.FREEHAND;
    private Color currentColor = Color.BLACK;
    private double lineWidth = 2.0;

    // Drawing state variables
    private double startX, startY;

    public SketchPadPlugin() {}

    @Override
    public String getPluginId() {
        return "sketchpad.textwrenchplugin";
    }

    @Override
    public String getPluginName() {
        return "Enhanced Sketch Pad Plugin";
    }

    @Override
    public String getPluginDescription() {
        return "TextWrench plugin for advanced sketching with multiple shape drawing modes and color options";
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;

        // Create main menu items for sketch pad
        MenuItem openSketchPadMenuItem = new MenuItem("New Sketch Pad");
        openSketchPadMenuItem.setOnAction(e -> openSketchPadInNewTab());
        context.addMenuItem("Sketchpad", openSketchPadMenuItem);

        // Shape mode menu
        Menu shapeModeMenu = new Menu("Drawing Mode");
        ToggleGroup modeToggleGroup = new ToggleGroup();

        RadioMenuItem freehandItem = new RadioMenuItem("Freehand");
        RadioMenuItem rectangleItem = new RadioMenuItem("Rectangle");
        RadioMenuItem circleItem = new RadioMenuItem("Circle");
        RadioMenuItem triangleItem = new RadioMenuItem("Triangle");
        RadioMenuItem lineItem = new RadioMenuItem("Line");

        freehandItem.setToggleGroup(modeToggleGroup);
        rectangleItem.setToggleGroup(modeToggleGroup);
        circleItem.setToggleGroup(modeToggleGroup);
        triangleItem.setToggleGroup(modeToggleGroup);
        lineItem.setToggleGroup(modeToggleGroup);

        freehandItem.setOnAction(e -> currentMode = DrawMode.FREEHAND);
        rectangleItem.setOnAction(e -> currentMode = DrawMode.RECTANGLE);
        circleItem.setOnAction(e -> currentMode = DrawMode.CIRCLE);
        triangleItem.setOnAction(e -> currentMode = DrawMode.TRIANGLE);
        lineItem.setOnAction(e -> currentMode = DrawMode.LINE);

        freehandItem.setSelected(true);

        shapeModeMenu.getItems().addAll(
                freehandItem, rectangleItem, circleItem,
                triangleItem, lineItem
        );
        context.addMenuItem("Sketchpad", shapeModeMenu);

        // Color selection menu
        Menu colorMenu = new Menu("Color");
        MenuItem blackColor = new MenuItem("Black");
        MenuItem redColor = new MenuItem("Red");
        MenuItem blueColor = new MenuItem("Blue");
        MenuItem greenColor = new MenuItem("Green");

        blackColor.setOnAction(e -> currentColor = Color.BLACK);
        redColor.setOnAction(e -> currentColor = Color.RED);
        blueColor.setOnAction(e -> currentColor = Color.BLUE);
        greenColor.setOnAction(e -> currentColor = Color.GREEN);

        colorMenu.getItems().addAll(blackColor, redColor, blueColor, greenColor);
        context.addMenuItem("Sketchpad", colorMenu);

        // Line width menu
        Menu lineWidthMenu = new Menu("Line Width");
        MenuItem thinLine = new MenuItem("Thin");
        MenuItem mediumLine = new MenuItem("Medium");
        MenuItem thickLine = new MenuItem("Thick");

        thinLine.setOnAction(e -> lineWidth = 1.0);
        mediumLine.setOnAction(e -> lineWidth = 2.0);
        thickLine.setOnAction(e -> lineWidth = 4.0);

        lineWidthMenu.getItems().addAll(thinLine, mediumLine, thickLine);
        context.addMenuItem("Sketchpad", lineWidthMenu);
    }

    private void openSketchPadInNewTab() {
        Canvas newCanvas = new Canvas(800, 600);
        GraphicsContext gc = newCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

        // Add mouse event handlers for drawing
        newCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            startX = event.getX();
            startY = event.getY();
            lastX = startX;
            lastY = startY;
        });

        newCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            GraphicsContext canvasGc = newCanvas.getGraphicsContext2D();
            double x = event.getX();
            double y = event.getY();

            // Configure graphics context
            canvasGc.setStroke(currentColor);
            canvasGc.setLineWidth(lineWidth);
            canvasGc.setFill(currentColor);

            // Draw based on current mode
            switch (currentMode) {
                case FREEHAND:
                    canvasGc.strokeLine(lastX, lastY, x, y);
                    lastX = x;
                    lastY = y;
                    break;
                case RECTANGLE:
                    // Clear previous preview
                    canvasGc.clearRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

                    // Draw rectangle
                    double width = Math.abs(x - startX);
                    double height = Math.abs(y - startY);
                    double rectX = Math.min(startX, x);
                    double rectY = Math.min(startY, y);
                    canvasGc.strokeRect(rectX, rectY, width, height);
                    break;
                case CIRCLE:
                    // Clear previous preview
                    canvasGc.clearRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

                    // Draw circle
                    double radius = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                    canvasGc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    break;
                case TRIANGLE:
                    // Clear previous preview
                    canvasGc.clearRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

                    // Draw triangle
                    double[] xPoints = {startX, x, (startX + x) / 2};
                    double[] yPoints = {startY, startY, y};
                    canvasGc.strokePolygon(xPoints, yPoints, 3);
                    break;
                case LINE:
                    // Clear previous preview
                    canvasGc.clearRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

                    // Draw line
                    canvasGc.strokeLine(startX, startY, x, y);
                    break;
            }
        });

        // Mouse release to finalize drawing
        newCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            GraphicsContext canvasGc = newCanvas.getGraphicsContext2D();
            double x = event.getX();
            double y = event.getY();

            // Finalize shape based on current mode
            switch (currentMode) {
                case RECTANGLE:
                    double width = Math.abs(x - startX);
                    double height = Math.abs(y - startY);
                    double rectX = Math.min(startX, x);
                    double rectY = Math.min(startY, y);
                    canvasGc.strokeRect(rectX, rectY, width, height);
                    break;
                case CIRCLE:
                    double radius = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                    canvasGc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    break;
                case TRIANGLE:
                    double[] xPoints = {startX, x, (startX + x) / 2};
                    double[] yPoints = {startY, startY, y};
                    canvasGc.strokePolygon(xPoints, yPoints, 3);
                    break;
                case LINE:
                    canvasGc.strokeLine(startX, startY, x, y);
                    break;
            }
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