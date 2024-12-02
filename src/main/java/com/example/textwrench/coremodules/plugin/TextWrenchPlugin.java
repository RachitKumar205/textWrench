package com.example.textwrench.coremodules.plugin;

import javafx.scene.control.MenuItem;
import javafx.scene.Node;

/**
 * Core interface for TextWrench plugins
 */
public interface TextWrenchPlugin {
    /**
     * Unique identifier for the plugin
     * @return Plugin's unique ID
     */
    String getPluginId();

    /**
     * Human-readable name of the plugin
     * @return Plugin name
     */
    String getPluginName();

    /**
     * Detailed description of the plugin's functionality
     * @return Plugin description
     */
    String getPluginDescription();

    /**
     * Initialize the plugin with the editor context
     * @param context Plugin context for interaction with the editor
     */
    void initialize(PluginContext context);

    /**
     * Get the plugin's configuration options
     * @return Plugin configuration node
     */
    Node getConfigurationPane();

    /**
     * Check if the plugin is currently enabled
     * @return true if plugin is active
     */
    boolean isEnabled();

    /**
     * Enable or disable the plugin
     * @param enabled New plugin state
     */
    void setEnabled(boolean enabled);
}