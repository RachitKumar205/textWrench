package com.example.textwrench.model;

import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectItem {
    private String name;
    private File file;
    private boolean isDirectory;
    private List<ProjectItem> children;
    private Node icon;

    public ProjectItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
        this.children = new ArrayList<>();
        this.icon = determineIcon();
    }

    private Node determineIcon() {
        if (isDirectory) {
            // Folder icon in a muted color
            return createIcon(Material.FOLDER, "#607D8B");
        } else {
            String fileName = name.toLowerCase();
            if (fileName.endsWith(".txt")) {
                // Text file icon in blue
                return createIcon(Material.NOTE, "#2196F3");
            } else {
                // Generic file icon in gray
                return createIcon(Material.INSERT_DRIVE_FILE, "#9E9E9E");
            }
        }
    }

    private Node createIcon(Enum<?> iconCode, String color) {
        FontIcon icon = new FontIcon((Ikon) iconCode);
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.valueOf(color));
        return icon;
    }

    public void addChild(ProjectItem child) {
        children.add(child);
    }

    public List<ProjectItem> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Node getIcon() {
        return icon;
    }
}