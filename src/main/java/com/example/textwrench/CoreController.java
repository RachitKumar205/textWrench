package com.example.textwrench;

import com.example.textwrench.IconLoader.IconConfigLoader;
import com.example.textwrench.coremodules.FileManagementService;
import com.example.textwrench.coremodules.ProjectManagementService;
import com.example.textwrench.coremodules.TabManagementService;
import com.example.textwrench.coremodules.UIUtilityService;
import com.example.textwrench.coremodules.model.ProjectItem;
import com.example.textwrench.coremodules.plugin.PluginContext;
import com.example.textwrench.coremodules.plugin.PluginManager;
import com.example.textwrench.ui.CustomTreeCell;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private MenuItem newFileItem;

    @FXML
    private MenuItem openProjectItem;

    @FXML
    private MenuItem closeProjectItem;

    @FXML
    private MenuItem openFileItem;

    @FXML
    private MenuItem saveFileItem;

    @FXML
    private MenuItem exitItem;

    @FXML
    private Label projectExplorerLabel;

    @FXML
    private TabPane leftTabPane;

    @FXML
    private SplitPane parentSplitPane;

    @FXML
    private VBox pluginManagerVBox;

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

        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.loadPlugins(createPluginContext());
        try {
            VBox pluginManagerContent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/textwrench/plugin-manager.fxml")));
            VBox.setVgrow(pluginManagerContent, Priority.ALWAYS); // Ensure the VBox grows with the parent container
            pluginManagerVBox.getChildren().add(pluginManagerContent);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Setup left tab pane
        setupLeftTabPane();

        // Create initial tab
        createNewFile();
    }

    public BooleanProperty isProjectOpenProperty() {
        return isProjectOpen;
    }

    private PluginContext createPluginContext() {
        return new PluginContext() {
            @Override
            public Tab getCurrentTab() {
                return tabPane.getSelectionModel().getSelectedItem();
            }

            @Override
            public void addMenuItem(String menuTitle, MenuItem menuItem) {
                Menu menu = findOrCreateMenu(menuTitle);
                menu.getItems().add(menuItem);
            }

            @Override
            public Stage getPrimaryStage() {
                return (Stage) tabPane.getScene().getWindow();
            }

            @Override
            public void showNotification(String message) {
                statusBar.setText(message);
            }

            @Override
            public String getCurrentTabContent() {
                Tab currentTab = getCurrentTab();
                return currentTab.getText();
            }

            @Override
            public void setCurrentTabContent(Node content) {
                Tab currentTab = getCurrentTab();
                if (currentTab != null) {
                    currentTab.setContent(content);
                }
            }

            @Override
            public void addTab(Tab tab) {
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tab);
            }
        };
    }

    private Menu findOrCreateMenu(String menuTitle){
        for (Menu menu : menuBar.getMenus()) {
            if (menu.getText().equals(menuTitle)){
                return menu;
            }
        }

        Menu newMenu = new Menu(menuTitle);
        menuBar.getMenus().add(newMenu);
        return newMenu;
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+N: New File
        newFileItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));

        // Ctrl+O: Open Project
        openProjectItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));

        // Ctrl+S: Save File
        saveFileItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        // Ctrl+W: Close Project
        closeProjectItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

        // Ctrl+Q: Exit
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    }

    // Store content for each tab to support dynamic restoration
    private Map<Tab, Node> tabContentMap = new HashMap<>();
    private Tab activeTab = null; // Store the currently active tab
    private double previousDividerPosition = 0.20;

    // Initialize tab management
    private void setupLeftTabPane() {
        // Populate initial content map
        for (Tab tab : leftTabPane.getTabs()) {
            tabContentMap.put(tab, tab.getContent());
        }

        activeTab = leftTabPane.getTabs().getFirst();

        // Minimum width to show tab headers
        leftTabPane.setMinWidth(35);

        // Add sophisticated tab toggle listener
        leftTabPane.setOnMouseClicked(event -> {
            Tab selectedTab = leftTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                toggleTabContent(selectedTab);
            }
        });
    }

    private void toggleTabContent(Tab selectedTab) {
        // If the selected tab is the same as the active one, toggle its content visibility
        if (selectedTab == activeTab) {
            // Check if tab content is currently visible
            boolean isContentVisible = selectedTab.getContent() != null;

            if (isContentVisible) {

                previousDividerPosition = parentSplitPane.getDividerPositions()[0];
                // Hide content while preserving original
                selectedTab.setContent(null);
                parentSplitPane.setDividerPositions(0);
            } else {
                // Restore original content
                Node originalContent = tabContentMap.get(selectedTab);
                selectedTab.setContent(originalContent);
                parentSplitPane.setDividerPositions(previousDividerPosition);
            }
        } else {
            // If a different tab is selected, hide the previous active tab's content (if any)
            if (activeTab != null) {
                // Hide content of the previous active tab
                activeTab.setContent(null);
            }

            // Show content for the new selected tab
            Node originalContent = tabContentMap.get(selectedTab);
            selectedTab.setContent(originalContent);
            parentSplitPane.setDividerPositions(previousDividerPosition);

            // Update the active tab
            activeTab = selectedTab;
        }
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

        if (projectManagementService.getCurrentProject() != null) {
            isProjectOpen.set(true);

            // Close the default tab if present
            if (!tabPane.getTabs().isEmpty()) {
                Tab defaultTab = tabPane.getTabs().get(0);
                if ("Untitled".equals(defaultTab.getText())) { // Check if the tab is the default one
                    tabPane.getTabs().remove(defaultTab);
                }
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