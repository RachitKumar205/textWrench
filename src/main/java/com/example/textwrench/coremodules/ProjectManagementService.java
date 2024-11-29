package com.example.textwrench.coremodules;

import com.example.textwrench.coremodules.model.ProjectItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static com.example.textwrench.coremodules.service.ProjectService.loadProject;

public class ProjectManagementService {
    private TreeView<ProjectItem> projectExplorer;
    private ProjectItem currentProject;
    private TabPane tabPane;
    private TabManagementService tabManagementService;
    private UIUtilityService uiUtilityService;
    private FileManagementService fileManagementService;

    public ProjectManagementService(TreeView<ProjectItem> projectExplorer, TabPane tabPane) {
        this.projectExplorer = projectExplorer;
        this.tabPane = tabPane;
        this.tabManagementService = new TabManagementService(tabPane);
        this.uiUtilityService = new UIUtilityService();
        this.fileManagementService = new FileManagementService(tabPane);
    }

    public ProjectItem getCurrentProject() {
        return currentProject;
    }

    public void openProject(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Folder");

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            // Load project structure
            this.currentProject = loadProject(selectedDirectory);

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

    public void closeProject() {
        if (projectExplorer != null) {
            // Check for unsaved changes in tabs before closing project
            if (!tabManagementService.closeAllTabsWithSaveCheck()) {
                return; // Cancel closing project if any tab has unsaved changes
            }

            // Clear the project explorer
            projectExplorer.setRoot(null);
            this.currentProject = null;

            // Create a new empty tab
            tabPane.getTabs().add(tabManagementService.createNewTab("Untitled", null));
        }
    }

    public void openProjectFile(ProjectItem projectItem) {
        try {
            File file = projectItem.getFile();
            String content = java.nio.file.Files.readString(file.toPath());

            // Check if file is already open in a tab
            var existingTab = tabPane.getTabs().stream()
                    .filter(tab -> tab.getUserData() != null && tab.getUserData().equals(file))
                    .findFirst();

            if (existingTab.isPresent()) {
                // If file is already open, switch to that tab
                tabPane.getSelectionModel().select(existingTab.get());
            } else {
                // Create a new tab for the file
                var newTab = tabManagementService.createNewTab(file.getName(), content);
                newTab.setUserData(file);
                tabPane.getTabs().add(newTab);
                tabPane.getSelectionModel().select(newTab);
            }
        } catch (Exception e) {
            uiUtilityService.showAlert("Error", "Could not open file: " + e);
        }
    }
}