package com.example.textwrench;

import com.example.textwrench.coremodules.plugin.PluginManager;
import com.example.textwrench.coremodules.plugin.TextWrenchPlugin;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PluginManagerController {

    @FXML
    private VBox pluginContainer;

    private PluginManager pluginManager;

    @FXML
    public void initialize() {
        pluginManager = PluginManager.getInstance();
        loadPlugins();
    }

    @FXML
    public void reloadPlugins() {
        // Potentially reload plugins from disk or refresh plugin states
        loadPlugins();
    }

    private void loadPlugins() {
        // Clear any existing plugins
        pluginContainer.getChildren().clear();

        // Add each plugin as a card
        for (TextWrenchPlugin plugin : pluginManager.getPlugins()) {
            addPluginCard(plugin);
        }
    }

    private void addPluginCard(TextWrenchPlugin plugin) {
        // Create card container
        VBox card = new VBox(10);
        card.getStyleClass().add("plugin-card");

        // Plugin name label
        Label nameLabel = new Label(plugin.getPluginName());
        nameLabel.getStyleClass().add("plugin-card-header");

        //Plugin ID label
        Label idLabel = new Label(plugin.getPluginId());
        idLabel.getStyleClass().add("plugin-card-id");

        // Plugin desc label
        Label descLabel = new Label(plugin.getPluginDescription());
        descLabel.getStyleClass().add("plugin-card-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(300);

        // Status label
        Label statusLabel = new Label(plugin.isEnabled() ? "Enabled" : "Disabled");
        if (plugin.isEnabled()) {
            statusLabel.getStyleClass().add("plugin-card-status-enabled");
        } else {
            statusLabel.getStyleClass().add("plugin-card-status-disabled");
        }

        // Toggle button
        Button toggleButton = new Button(plugin.isEnabled() ? "Disable" : "Enable");
        toggleButton.getStyleClass().add("plugin-card-button");
        toggleButton.setOnAction(event -> {
            // Toggle plugin state
            plugin.setEnabled(!plugin.isEnabled());

            // Update button and status label
            toggleButton.setText(plugin.isEnabled() ? "Disable" : "Enable");
            statusLabel.setText(plugin.isEnabled() ? "Enabled" : "Disabled");
        });

        // Horizontal layout for button and status
        HBox actionBox = new HBox(10, statusLabel, toggleButton);
        actionBox.setStyle("-fx-alignment: center-right;");

        // Combine elements
        card.getChildren().addAll(nameLabel, idLabel, descLabel, actionBox);

        // Add to container
        pluginContainer.getChildren().add(card);
    }
}