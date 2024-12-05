package com.example.textwrench.coremodules;

import com.example.textwrench.IconLoader.IconConfigLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

public class TabManagementService {
    private TabPane tabPane;
    private UIUtilityService uiUtilityService;

    public TabManagementService(TabPane tabPane) {
        this.tabPane = tabPane;
        this.uiUtilityService = new UIUtilityService();
    }

    public Tab createNewTab(String tabName, String content) {
        CodeArea codeArea = new CodeArea();
        codeArea.setId("codeArea");

        // Wrap the CodeArea in a VirtualizedScrollPane
        VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(codeArea);
        scrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("virtual-scroll-pane.css")).toExternalForm());

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // If content is provided, set it
        if (content != null) {
            codeArea.replaceText(content);
        }

        Node iconNode = new IconConfigLoader().determineFileIcon(tabName);

        Tab tab = new Tab(tabName);
        tab.setContent(scrollPane);
        tab.setGraphic(iconNode);

        // Add close request handler
        tab.setOnCloseRequest(event -> {
            if (!isTabContentSaved(tab)) {
                event.consume(); // Prevent tab from closing
            }
        });

        return tab;
    }

    public boolean isTabContentSaved(Tab tab) {
        // Check if content is a VirtualizedScrollPane with CodeArea (existing logic)
        if (tab.getContent() instanceof VirtualizedScrollPane) {
            VirtualizedScrollPane scrollPane = (VirtualizedScrollPane) tab.getContent();
            CodeArea codeArea = (CodeArea) scrollPane.getContent();
            File associatedFile = (File) tab.getUserData();

            if (codeArea.getText().isEmpty()) return true;

            if (associatedFile == null || !codeArea.getText().equals(readFileContent(associatedFile))) {
                return handleUnsavedChanges(tab);
            }
            return true;
        }

        // For other types of tabs (like SketchPad), always allow closing
        return true;
    }

    private boolean handleUnsavedChanges(Tab tab) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Unsaved Changes");
        confirmAlert.setHeaderText("Do you want to save changes to " + tab.getText() + "?");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType discardButton = new ButtonType("Discard");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmAlert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.get() == saveButton) {
            new FileManagementService(tabPane).saveFile();
            return true;
        } else return result.get() == discardButton;
    }

    private String readFileContent(File file) {
        try {
            return file != null ? new String(Files.readAllBytes(file.toPath())) : "";
        } catch (IOException e) {
            return "";
        }
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void addCaretPositionListener(UIUtilityService uiUtilityService, Label statusBar) {
        CodeArea codeArea = getCurrentCodeArea();
        if (codeArea != null) {
            codeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                uiUtilityService.updateStatusBar(this, statusBar);
            });
        }
    }

    public CodeArea getCurrentCodeArea() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            VirtualizedScrollPane scrollPane = (VirtualizedScrollPane) currentTab.getContent();
            return (CodeArea) scrollPane.getContent();
        }
        return null;
    }

    public boolean closeAllTabsWithSaveCheck() {
        for (Tab tab : tabPane.getTabs()) {
            if (!isTabContentSaved(tab)) {
                return false; // Cancel closing if any tab has unsaved changes
            }
        }
        tabPane.getTabs().clear();
        return true;
    }
}