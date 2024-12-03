package com.example.textwrench.coremodules.service;

import com.example.textwrench.coremodules.TabManagementService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.fxmisc.richtext.CodeArea;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FindReplaceService {
    private TabManagementService tabManagementService;

    public FindReplaceService(TabManagementService tabManagementService) {
        this.tabManagementService = tabManagementService;
    }

    public void showFindReplaceDialog() {
        CodeArea currentCodeArea = tabManagementService.getCurrentCodeArea();
        if (currentCodeArea == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Find and Replace");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Create dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField findField = new TextField();
        findField.setPromptText("Find");
        TextField replaceField = new TextField();
        replaceField.setPromptText("Replace");

        CheckBox caseSensitiveCheck = new CheckBox("Case Sensitive");
        CheckBox wholeWordCheck = new CheckBox("Whole Word");

        grid.add(new Label("Find:"), 0, 0);
        grid.add(findField, 1, 0);
        grid.add(new Label("Replace:"), 0, 1);
        grid.add(replaceField, 1, 1);
        grid.add(caseSensitiveCheck, 0, 2);
        grid.add(wholeWordCheck, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Create custom buttons
        ButtonType findButton = new ButtonType("Find", ButtonBar.ButtonData.OK_DONE);
        ButtonType replaceButton = new ButtonType("Replace", ButtonBar.ButtonData.APPLY);
        ButtonType replaceAllButton = new ButtonType("Replace All", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(findButton, replaceButton, replaceAllButton, cancelButton);

        // Event handling for buttons
        dialog.setResultConverter(buttonType -> {
            if (buttonType == findButton) {
                findText(currentCodeArea, findField.getText(),
                        caseSensitiveCheck.isSelected(),
                        wholeWordCheck.isSelected());
            } else if (buttonType == replaceButton) {
                replaceNext(currentCodeArea,
                        findField.getText(),
                        replaceField.getText(),
                        caseSensitiveCheck.isSelected(),
                        wholeWordCheck.isSelected());
            } else if (buttonType == replaceAllButton) {
                replaceAll(currentCodeArea,
                        findField.getText(),
                        replaceField.getText(),
                        caseSensitiveCheck.isSelected(),
                        wholeWordCheck.isSelected());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void findText(CodeArea codeArea, String searchText,
                          boolean caseSensitive, boolean wholeWord) {
        if (searchText.isEmpty()) return;

        String text = codeArea.getText();
        int startIndex = codeArea.getCaretPosition();

        // Adjust regex based on whole word and case sensitivity
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        String searchPattern = wholeWord ?
                "\\b" + Pattern.quote(searchText) + "\\b" :
                Pattern.quote(searchText);

        Pattern pattern = Pattern.compile(searchPattern, flags);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find(startIndex)) {
            codeArea.selectRange(matcher.start(), matcher.end());
            codeArea.requestFollowCaret();
        } else {
            // Wrap around to start if not found
            if (matcher.find(0)) {
                codeArea.selectRange(matcher.start(), matcher.end());
                codeArea.requestFollowCaret();
            } else {
                showAlert("Text not found", "No matches found for: " + searchText);
            }
        }
    }

    private void replaceNext(CodeArea codeArea, String findText,
                             String replaceText, boolean caseSensitive,
                             boolean wholeWord) {
        if (findText.isEmpty()) return;

        String text = codeArea.getText();
        int startIndex = codeArea.getCaretPosition();

        // Adjust regex based on whole word and case sensitivity
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        String searchPattern = wholeWord ?
                "\\b" + Pattern.quote(findText) + "\\b" :
                Pattern.quote(findText);

        Pattern pattern = Pattern.compile(searchPattern, flags);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find(startIndex)) {
            codeArea.replaceText(matcher.start(), matcher.end(), replaceText);
            findText(codeArea, findText, caseSensitive, wholeWord);
        } else {
            // Wrap around to start if not found
            if (matcher.find(0)) {
                codeArea.replaceText(matcher.start(), matcher.end(), replaceText);
                findText(codeArea, findText, caseSensitive, wholeWord);
            } else {
                showAlert("Text not found", "No matches found for: " + findText);
            }
        }
    }

    private void replaceAll(CodeArea codeArea, String findText,
                            String replaceText, boolean caseSensitive,
                            boolean wholeWord) {
        if (findText.isEmpty()) return;

        String text = codeArea.getText();

        // Adjust regex based on whole word and case sensitivity
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        String searchPattern = wholeWord ?
                "\\b" + Pattern.quote(findText) + "\\b" :
                Pattern.quote(findText);

        Pattern pattern = Pattern.compile(searchPattern, flags);

        // Replace all matches
        String newText = pattern.matcher(text).replaceAll(replaceText);

        // Set the entire text with replacements
        codeArea.replaceText(0, text.length(), newText);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}