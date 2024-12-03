package com.example.textwrench.coremodules;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

public class FindReplaceController {

    @FXML
    private TextField findField;

    @FXML
    private TextField replaceField;

    @FXML
    private Button findNextButton;

    @FXML
    private Button findPreviousButton;

    @FXML
    private Button replaceButton;

    @FXML
    private Button replaceAllButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox findReplacePanel;

    private TabManagementService tabManagementService;
    private int lastFoundIndex = -1;
    private String lastSearchText = "";

    public FindReplaceController(){}

    public FindReplaceController(TabManagementService tabManagementService) {
        this.tabManagementService = tabManagementService;
    }

    public void setTabManagementService(TabManagementService tabManagementService) {
        this.tabManagementService = tabManagementService;
    }

    @FXML
    private void initialize() {
        // Clear status when fields change
        findField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
        replaceField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
    }

    private void clearStatus() {
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @FXML
    private void handleFindNext() {
        String searchText = findField.getText();
        if (searchText.isEmpty()) {
            updateStatus("Please enter text to find.");
            return;
        }

        CodeArea codeArea = tabManagementService.getCurrentCodeArea();
        if (codeArea == null) {
            updateStatus("No active tab to search in.");
            return;
        }

        String content = codeArea.getText();
        int currentCaret = codeArea.getCaretPosition();

        // Reset search if search text changed
        if (!searchText.equals(lastSearchText)) {
            lastFoundIndex = -1;
            lastSearchText = searchText;
        }

        // Start searching from the current caret position
        int nextIndex = content.indexOf(searchText, currentCaret);

        // If not found from current position, wrap around to the beginning
        if (nextIndex == -1) {
            nextIndex = content.indexOf(searchText);
        }

        if (nextIndex != -1) {
            codeArea.selectRange(nextIndex, nextIndex + searchText.length());
            codeArea.requestFollowCaret();
            lastFoundIndex = nextIndex;

            // Calculate line number and index within the line
            int lineNumber = codeArea.getText(0, nextIndex).split("\n").length;
            int lineStartIndex = content.lastIndexOf('\n', nextIndex - 1) + 1;
            int indexInLine = nextIndex - lineStartIndex;
            updateStatus("Found at line " + lineNumber + ":" + indexInLine);
        } else {
            updateStatus("Text not found.");
        }
    }

    @FXML
    private void handleFindPrevious() {
        String searchText = findField.getText();
        if (searchText.isEmpty()) {
            updateStatus("Please enter text to find.");
            return;
        }

        CodeArea codeArea = tabManagementService.getCurrentCodeArea();
        if (codeArea == null) {
            updateStatus("No active tab to search in.");
            return;
        }

        String content = codeArea.getText();
        int currentCaret = codeArea.getCaretPosition();

        // Reset search if search text changed
        if (!searchText.equals(lastSearchText)) {
            lastFoundIndex = content.length();
            lastSearchText = searchText;
        }

        // Start searching backwards from current caret position
        int previousIndex = content.lastIndexOf(searchText, currentCaret - 1);

        // If not found from current position, wrap around to the end
        if (previousIndex == -1) {
            previousIndex = content.lastIndexOf(searchText);
        }

        if (previousIndex != -1) {
            codeArea.selectRange(previousIndex, previousIndex + searchText.length());
            codeArea.requestFollowCaret();
            lastFoundIndex = previousIndex;

            // Calculate line number and index within the line
            int lineNumber = codeArea.getText(0, previousIndex).split("\n").length;
            int lineStartIndex = content.lastIndexOf('\n', previousIndex - 1) + 1;
            int indexInLine = previousIndex - lineStartIndex;
            updateStatus("Found at line " + lineNumber + ":" + indexInLine);
        } else {
            updateStatus("Text not found.");
        }
    }

    @FXML
    private void handleReplace() {
        String searchText = findField.getText();
        String replacementText = replaceField.getText();
        if (searchText.isEmpty()) {
            updateStatus("Please enter text to find.");
            return;
        }

        CodeArea codeArea = tabManagementService.getCurrentCodeArea();
        if (codeArea == null) {
            updateStatus("No active tab to search in.");
            return;
        }

        int start = codeArea.getSelection().getStart();
        int end = codeArea.getSelection().getEnd();
        String selectedText = codeArea.getSelectedText();

        if (selectedText.equals(searchText)) {
            codeArea.replaceText(start, end, replacementText);
            updateStatus("Replaced one occurrence.");
            // Trigger find next to continue search
            handleFindNext();
        } else {
            updateStatus("Selected text does not match the search text.");
        }
    }

    @FXML
    private void handleReplaceAll() {
        String searchText = findField.getText();
        String replacementText = replaceField.getText();
        if (searchText.isEmpty()) {
            updateStatus("Please enter text to find.");
            return;
        }

        CodeArea codeArea = tabManagementService.getCurrentCodeArea();
        if (codeArea == null) {
            updateStatus("No active tab to search in.");
            return;
        }

        String content = codeArea.getText();
        String updatedContent = content.replace(searchText, replacementText);

        if (!content.equals(updatedContent)) {
            codeArea.replaceText(updatedContent);
            updateStatus("Replaced all occurrences.");
        } else {
            updateStatus("No occurrences found to replace.");
        }
    }

    @FXML
    private void handleClose() {
        // Remove the Find and Replace panel from its parent
        ((VBox) findReplacePanel.getParent()).getChildren().remove(findReplacePanel);
    }
}