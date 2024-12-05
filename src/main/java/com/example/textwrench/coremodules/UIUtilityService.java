package com.example.textwrench.coremodules;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;

public class UIUtilityService {
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateStatusBar(TabManagementService tabManagementService, Label statusBar) {
        CodeArea currentCodeArea = tabManagementService.getCurrentCodeArea();
        if (currentCodeArea != null) {
            int lineCount = currentCodeArea.getParagraphs().size();
            int charCount = currentCodeArea.getLength();
            int caretPosition = currentCodeArea.getCaretPosition();
            int lineNumber = currentCodeArea.getText(0, caretPosition).split("\n").length;
            int lineStartIndex = currentCodeArea.getText().lastIndexOf('\n', caretPosition - 1) + 1;
            int indexInLine = caretPosition - lineStartIndex;
            String firstLine = currentCodeArea.getText().split("\n")[0];
            statusBar.setText("Ready");
        } else {
            statusBar.setText("Ready");
        }
    }
}