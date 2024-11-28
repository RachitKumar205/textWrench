package com.example.textwrench.coremodules;

import javafx.scene.control.Alert;

public class UIUtilityService {
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateStatusBar(TabManagementService tabManagementService, javafx.scene.control.Label statusBar) {
        var currentCodeArea = tabManagementService.getCurrentCodeArea();
        if (currentCodeArea != null) {
            int lineCount = currentCodeArea.getText().split("\n").length;
            int charCount = currentCodeArea.getText().length();
            statusBar.setText("Lines: " + lineCount + " | Characters: " + charCount);
        } else {
            statusBar.setText("Ready");
        }
    }
}