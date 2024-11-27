package com.example.textwrench;

import com.example.textwrench.model.ProjectItem;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.textwrench.service.ProjectService.loadProject;

public class TextEditorController {
    @FXML
    private CodeArea codeArea;

    @FXML
    private Label statusBar;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TabPane tabPane;

    @FXML
    private TreeView<ProjectItem> projectExplorer;

    private ProjectItem currentProject;

    private File currentFile;

    private ExecutorService executor;

    @FXML
    public void initialize() {
        executor = Executors.newSingleThreadExecutor();
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
        menuBar.useSystemMenuBarProperty().set(true);
        setupProjectExplorer();

        createNewFile();
    }

    private CodeArea getCurrentCodeArea() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        return currentTab != null ? (CodeArea) currentTab.getContent() : null;
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+N: New File
        MenuItem newFileItem = new MenuItem("New");
        newFileItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newFileItem.setOnAction(e -> createNewFile());

        // Ctrl+O: Open File
        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(e -> openFile());

        // Ctrl+S: Save File
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(e -> saveFile());
    }

    private Tab createNewTab(String tabName, String content) {
        CodeArea codeArea = new CodeArea();
        codeArea.setId("codeArea");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // Line number and other setup
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStyleClass().addAll("text-area");

        // Add status bar update listener
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            updateStatusBar();
        });

        // If content is provided, set it
        if (content != null) {
            codeArea.replaceText(content);
        }

        Tab tab = new Tab(tabName);
        tab.setContent(codeArea);

        // Add close request handler
        tab.setOnCloseRequest(event -> {
            if (!isTabContentSaved(tab)) {
                event.consume(); // Prevent tab from closing
            }
        });

        return tab;
    }

    @FXML
    public void createNewFile() {
        Tab newTab = createNewTab("Untitled", null);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        updateStatusBar();
    }

    @FXML
    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                String content = new String(Files.readAllBytes(selectedFile.toPath()));

                // Check if file is already open in a tab
                Optional<Tab> existingTab = tabPane.getTabs().stream()
                        .filter(tab -> tab.getUserData() != null && tab.getUserData().equals(selectedFile))
                        .findFirst();

                if (existingTab.isPresent()) {
                    tabPane.getSelectionModel().select(existingTab.get());
                } else {
                    Tab newTab = createNewTab(selectedFile.getName(), content);
                    newTab.setUserData(selectedFile); // Store file reference
                    tabPane.getTabs().add(newTab);
                    tabPane.getSelectionModel().select(newTab);
                }
                updateStatusBar();
            } catch (IOException e) {
                showAlert("Error", "Could not read file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void saveFile() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) return;

        CodeArea codeArea = (CodeArea) currentTab.getContent();
        File fileToSave = (File) currentTab.getUserData();

        if (fileToSave == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Text File");
            fileToSave = fileChooser.showSaveDialog(null);
        }

        if (fileToSave != null) {
            try {
                Files.write(fileToSave.toPath(), codeArea.getText().getBytes());
                currentTab.setText(fileToSave.getName());
                currentTab.setUserData(fileToSave);
                updateStatusBar();
                showAlert("Success", "File saved successfully!");
            } catch (IOException e) {
                showAlert("Error", "Could not save file: " + e.getMessage());
            }
        }
    }

    private boolean isTabContentSaved(Tab tab) {
        CodeArea codeArea = (CodeArea) tab.getContent();
        File associatedFile = (File) tab.getUserData();

        // If the tab has no content, it can be closed
        if (codeArea.getText().isEmpty()) return true;

        // If the tab has unsaved changes, prompt user
        if (associatedFile == null || !codeArea.getText().equals(readFileContent(associatedFile))) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Unsaved Changes");
            confirmAlert.setHeaderText("Do you want to save changes to " + tab.getText() + "?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Discard");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirmAlert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.get() == saveButton) {
                saveFile();
                return true;
            } else return result.get() == discardButton;
        }
        return true;
    }

    private String readFileContent(File file) {
        try {
            return file != null ? new String(Files.readAllBytes(file.toPath())) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private void updateStatusBar() {
        CodeArea currentCodeArea = getCurrentCodeArea();
        if (currentCodeArea != null) {
            int lineCount = currentCodeArea.getText().split("\n").length;
            int charCount = currentCodeArea.getText().length();
            statusBar.setText("Lines: " + lineCount + " | Characters: " + charCount);
        } else {
            statusBar.setText("Ready");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleExit() {
        // Check for unsaved changes in all tabs
        for (Tab tab : tabPane.getTabs()) {
            if (!isTabContentSaved(tab)) {
                return; // Cancel exit if any tab has unsaved changes
            }
        }

        // Shutdown the executor service
        if (executor != null) {
            executor.shutdown();
        }

        Platform.exit();
    }

    private void setupProjectExplorer() {
        projectExplorer.setCellFactory(param -> new TreeCell<>() {
            @Override
            protected void updateItem(ProjectItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());

                    // Use Ikonli Material icons
                    FontIcon icon;
                    if (item.isDirectory()) {
                        icon = new FontIcon(Material.FOLDER);
                    } else {
                        icon = new FontIcon(Material.INSERT_DRIVE_FILE);
                    }

                    icon.setIconSize(16);
                    setGraphic(icon);
                }
            }
        });

        // Handle double-click to open files
        projectExplorer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<ProjectItem> selectedItem = projectExplorer.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.getValue().isDirectory()) {
                    openProjectFile(selectedItem.getValue());
                }
            }
        });
    }

    @FXML
    public void openProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Folder");

        // You might want to get the stage from the scene or pass it in
        Stage stage = (Stage) projectExplorer.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            // Load project structure
            currentProject = loadProject(selectedDirectory);

            // Create root tree item
            TreeItem<ProjectItem> rootItem = new TreeItem<>(currentProject);

            // Recursively build tree
            buildTreeView(rootItem, currentProject);

            // Set the root of the project explorer
            projectExplorer.setRoot(rootItem);
            rootItem.setExpanded(true);
        }
    }

    private void buildTreeView(TreeItem<ProjectItem> parentTreeItem, ProjectItem parentProjectItem) {
        for (ProjectItem childProjectItem : parentProjectItem.getChildren()) {
            TreeItem<ProjectItem> childTreeItem = new TreeItem<>(childProjectItem);
            parentTreeItem.getChildren().add(childTreeItem);

            // Recursively build for directories
            if (childProjectItem.isDirectory()) {
                buildTreeView(childTreeItem, childProjectItem);
            }
        }
    }

    private void openProjectFile(ProjectItem projectItem) {
        try {
            File file = projectItem.getFile();
            String content = java.nio.file.Files.readString(file.toPath());

            // Check if file is already open in a tab
            Optional<Tab> existingTab = tabPane.getTabs().stream()
                    .filter(tab -> tab.getUserData() != null && tab.getUserData().equals(file))
                    .findFirst();

            if (existingTab.isPresent()) {
                // If file is already open, switch to that tab
                tabPane.getSelectionModel().select(existingTab.get());
            } else {
                // Create a new tab for the file
                Tab newTab = createNewTab(file.getName(), content);
                newTab.setUserData(file);
                tabPane.getTabs().add(newTab);
                tabPane.getSelectionModel().select(newTab);
            }

            updateStatusBar();
        } catch (Exception e) {
            showAlert("Error", "Could not open file: " + e);
        }
    }

    @FXML
    public void closeProject() {
        if (projectExplorer != null) {
            // Check for unsaved changes in tabs before closing project
            for (Tab tab : tabPane.getTabs()) {
                if (!isTabContentSaved(tab)) {
                    return; // Cancel closing project if any tab has unsaved changes
                }
            }

            // Clear the project explorer
            projectExplorer.setRoot(null);
            currentProject = null;

            // Close all tabs
            tabPane.getTabs().clear();

            // Create a new empty tab
            createNewFile();
        }
    }
}