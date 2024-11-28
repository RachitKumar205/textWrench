package com.example.textwrench.coremodules;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class FileManagementService {
    private TabPane tabPane;
    private TabManagementService tabManagementService;
    private UIUtilityService uiUtilityService;

    public FileManagementService(TabPane tabPane) {
        this.tabPane = tabPane;
        this.tabManagementService = new TabManagementService(tabPane);
        this.uiUtilityService = new UIUtilityService();
    }

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
                    Tab newTab = tabManagementService.createNewTab(selectedFile.getName(), content);
                    newTab.setUserData(selectedFile); // Store file reference
                    tabPane.getTabs().add(newTab);
                    tabPane.getSelectionModel().select(newTab);
                }
            } catch (IOException e) {
                uiUtilityService.showAlert("Error", "Could not read file: " + e.getMessage());
            }
        }
    }

    public void saveFile() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) return;

        VirtualizedScrollPane scrollPane = (VirtualizedScrollPane) currentTab.getContent();
        CodeArea codeArea = (CodeArea) scrollPane.getContent();
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
                uiUtilityService.showAlert("Success", "File saved successfully!");
            } catch (IOException e) {
                uiUtilityService.showAlert("Error", "Could not save file: " + e.getMessage());
            }
        }
    }
}