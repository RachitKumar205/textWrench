package com.example.textwrench.plugins;

import com.example.textwrench.coremodules.plugin.PluginContext;
import com.example.textwrench.coremodules.plugin.TextWrenchPlugin;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

public class ExamplePlugin implements TextWrenchPlugin {
    private boolean enabled = true;
    private PluginContext context;

    public ExamplePlugin(){};

    @Override
    public String getPluginId() {
        return "example.textwrenchplugin";
    }

    @Override
    public String getPluginName() {
        return "Example Plugin";
    }

    @Override
    public String getPluginDescription() {
        return "Testing out plugin integration";
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;

        // Add Git menu items
        MenuItem commitMenuItem = new MenuItem("Commit Changes");
        commitMenuItem.setOnAction(e -> commitChanges());
        context.addMenuItem("Git", commitMenuItem);

        MenuItem pushMenuItem = new MenuItem("Push Changes");
        pushMenuItem.setOnAction(e -> pushChanges());
        context.addMenuItem("Git", pushMenuItem);
    }

    private void commitChanges() {
        String currentContent = context.getCurrentTabContent();
        // Implement basic Git commit logic
        context.showNotification("Committed current file changes");
    }

    private void pushChanges() {
        // Implement Git push logic
        context.showNotification("Pushed changes to remote repository");
    }

    @Override
    public Node getConfigurationPane() {
        VBox configPane = new VBox(10);
        // Add configuration options for Git plugin
        return configPane;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}