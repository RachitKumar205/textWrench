package com.example.textwrench.coremodules.plugin;
import javafx.scene.control.Tab;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Context interface providing plugins access to editor functionality
 */
public interface PluginContext {
    /**
     * Get the current active tab
     * @return Current active editor tab
     */
    Tab getCurrentTab();

    /**
     * Add a new menu item to the application menu bar
     * @param menuTitle Top-level menu title
     * @param menuItem Menu item to add
     */
    void addMenuItem(String menuTitle, MenuItem menuItem);

    /**
     * Get the main application stage
     * @return Primary application stage
     */
    Stage getPrimaryStage();

    /**
     * Show a notification to the user
     * @param message Notification message
     */
    void showNotification(String message);

    /**
     * Get the content of the current active tab
     * @return Content of the current tab
     */
    String getCurrentTabContent();

    /**
     * Set the content of the current active tab
     * @param content New content for the tab
     */
    void setCurrentTabContent(String content);
}
