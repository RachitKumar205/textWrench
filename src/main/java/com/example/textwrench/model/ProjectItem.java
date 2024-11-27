package com.example.textwrench.model;

import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectItem {
    private String name;
    private File file;
    private boolean isDirectory;
    private List<ProjectItem> children;
    private Node icon;
    private static Map<String, String> extensionToIconMap;

    static {
        // Load icon configuration from JSON
        loadIconConfiguration();
    }

    public ProjectItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
        this.children = new ArrayList<>();
        this.icon = determineIcon();
    }

    public static void loadIconConfiguration() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream configStream = ProjectItem.class.getClassLoader().getResourceAsStream("language-icons.json");

            if (configStream == null) {
                throw new IOException("Config file not found in resources folder.");
            }

            // Read the configuration into a Map
            extensionToIconMap = objectMapper.readValue(configStream, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            extensionToIconMap = Map.of(); // In case of error, use an empty map
        }
    }

    public Node determineIcon() {
        if (isDirectory) {
            // Folder icon in a muted color
            return createIcon(BootstrapIcons.FOLDER2, "#607D8B");
        } else {
            String fileName = name.toLowerCase();
            String extension = getFileExtension(fileName);
            String iconCode = extensionToIconMap.get(extension);
            if (iconCode != null) {
                // Icon found in config
                return createIcon(getIconFromString(iconCode), "#2196F3");
            } else {
                // Generic file icon in gray
                return createIcon(BootstrapIcons.FILE_EARMARK_CODE, "#ffffff");
            }
        }
    }

    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    public Node createIcon(Ikon iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);  // Directly passing the Ikon object
        icon.setIconSize(20);
        icon.setIconColor(javafx.scene.paint.Color.valueOf(color));
        return icon;
    }


    public Ikon getIconFromString(String iconCode) {
        try {
            // Directly map the icon code string to the Ikon
            if (iconCode.startsWith("Material.")) {
                return (Ikon) Enum.valueOf(Material.class, iconCode.substring(9));  // Remove "Material."
            } else if (iconCode.startsWith("BootstrapIcons.")) {
                return (Ikon) Enum.valueOf(BootstrapIcons.class, iconCode.substring(15));  // Remove "BootstrapIcons."
            } else {
                return BootstrapIcons.FILE_EARMARK_CODE;  // Fallback to a default icon
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return BootstrapIcons.FILE_EARMARK_CODE;  // Fallback icon in case of error
        }
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
