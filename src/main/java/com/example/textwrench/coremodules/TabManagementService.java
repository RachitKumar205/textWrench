package com.example.textwrench.coremodules;

import com.example.textwrench.IconLoader.IconConfigLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
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
        VirtualizedScrollPane scrollPane = (VirtualizedScrollPane) tab.getContent();
        CodeArea codeArea = (CodeArea) scrollPane.getContent();
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
                new FileManagementService(tabPane).saveFile();
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