package com.example.textwrench;

import com.example.textwrench.IconLoader.IconConfigLoader;
import com.example.textwrench.coremodules.FileManagementService;
import com.example.textwrench.coremodules.ProjectManagementService;
import com.example.textwrench.coremodules.TabManagementService;
import com.example.textwrench.coremodules.UIUtilityService;
import com.example.textwrench.coremodules.model.ProjectItem;
import com.example.textwrench.ui.CustomTreeCell;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreController {

    @FXML
    private Label statusBar;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TabPane tabPane;

    @FXML
    private TreeView<ProjectItem> projectExplorer;

    @FXML
    private Button openProjectButton;

    @FXML
    private Label projectExplorerLabel;

    private ExecutorService executor;
    private Map<String, ProjectItem.IconConfig> extensionToIconMap;

    private BooleanProperty isProjectOpen = new SimpleBooleanProperty(false);

    // Service classes
    private TabManagementService tabManagementService;
    private FileManagementService fileManagementService;
    private ProjectManagementService projectManagementService;
    private UIUtilityService uiUtilityService;

    @FXML
    public void initialize() {
        // Initialize executor and icon configuration
        executor = Executors.newSingleThreadExecutor();
        extensionToIconMap = IconConfigLoader.loadIconConfiguration();

        // Initialize service classes
        tabManagementService = new TabManagementService(tabPane);
        fileManagementService = new FileManagementService(tabPane);
        projectManagementService = new ProjectManagementService(projectExplorer, tabPane);
        uiUtilityService = new UIUtilityService();

        // Setup UI components
        setupKeyboardShortcuts();
        menuBar.useSystemMenuBarProperty().set(true);
        setupProjectExplorer();

        openProjectButton.visibleProperty().bind(isProjectOpen.not());

        // Create initial tab
        createNewFile();
    }

    public BooleanProperty isProjectOpenProperty() {
        return isProjectOpen;
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+N: New File
        MenuItem newFileItem = new MenuItem("New");
        newFileItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newFileItem.setOnAction(e -> createNewFile());

        // Ctrl+O: Open Project
        MenuItem openItem = new MenuItem("Open Project");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        openItem.setOnAction(e -> openProject());

        // Ctrl+S: Save File
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(e -> saveFile());

    }

    private void setupProjectExplorer() {
        projectExplorer.setCellFactory(param -> new TreeCell<>() {
            @Override
            protected void updateItem(ProjectItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;"); // Clear background color
                } else {
                    setText(item.getName());

                    // Use the icon from ProjectItem's determineIcon method
                    setGraphic(item.determineIcon());
                    setStyle(""); // Reset the style
                }
            }
        });

        // Disable selection and interaction when no project is loaded
        projectExplorer.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (projectManagementService.getCurrentProject() == null) {
                projectExplorer.getSelectionModel().clearSelection();
                projectExplorer.setStyle("-fx-background-color: transparent;"); // Clear background color
            } else {
                projectExplorer.setStyle(""); // Reset the style
            }
        });

        // Modify mouse click event to only work when a project is loaded
        projectExplorer.setOnMouseClicked(event -> {
            if (projectManagementService.getCurrentProject() == null) return; // Exit if no project is loaded

            if (event.getClickCount() == 2) {
                TreeItem<ProjectItem> selectedItem = projectExplorer.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.getValue().isDirectory()) {
                    projectManagementService.openProjectFile(selectedItem.getValue());
                }
            }
        });
    }

    @FXML
    public void createNewFile() {
        Tab newTab = tabManagementService.createNewTab("Untitled", null);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        uiUtilityService.updateStatusBar(tabManagementService, statusBar);
    }

    @FXML
    public void openFile() {
        fileManagementService.openFile();
        uiUtilityService.updateStatusBar(tabManagementService, statusBar);
    }

    @FXML
    public void saveFile() {
        fileManagementService.saveFile();
        uiUtilityService.updateStatusBar(tabManagementService, statusBar);
    }

    @FXML
    public void openProject() {
        Stage stage = (Stage) projectExplorer.getScene().getWindow();
        projectManagementService.openProject(stage);
        isProjectOpen.set(true);

        // Close the default tab if present
        if (!tabPane.getTabs().isEmpty()) {
            Tab defaultTab = tabPane.getTabs().getFirst();
            if ("Untitled".equals(defaultTab.getText())) { // Check if the tab is the default one
                tabPane.getTabs().remove(defaultTab);
            }
        }
    }

    @FXML
    public void closeProject() {
        projectManagementService.closeProject();
        isProjectOpen.set(false);
    }

    @FXML
    public void handleExit() {
        // Check for unsaved changes in all tabs
        for (Tab tab : tabPane.getTabs()) {
            if (!tabManagementService.isTabContentSaved(tab)) {
                return; // Cancel exit if any tab has unsaved changes
            }
        }

        // Shutdown the executor service
        if (executor != null) {
            executor.shutdown();
        }

        Platform.exit();
    }
}