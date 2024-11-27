package com.example.textwrench.model;

import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.devicons.Devicons;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ProjectItem {
    private String name;
    private File file;
    private boolean isDirectory;
    private List<ProjectItem> children;
    private Node icon;
    private static Map<String, IconConfig> extensionToIconMap;

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

            // Read the configuration into a JsonNode
            JsonNode rootNode = objectMapper.readTree(configStream);

            extensionToIconMap = new HashMap<>();

            // Iterate through each language and add to the map
            rootNode.fieldNames().forEachRemaining(language -> {
                JsonNode languageNode = rootNode.get(language);
                String iconClass = languageNode.get("class").asText();
                String color = languageNode.get("color").asText();
                extensionToIconMap.put(language, new IconConfig(iconClass, color));
            });

        } catch (IOException e) {
            e.printStackTrace();
            extensionToIconMap = new HashMap<>(); // In case of error, use an empty map
        }
    }

    public Node determineIcon() {
        if (isDirectory) {
            // Folder icon in a muted color
            return createIcon(BootstrapIcons.FOLDER2, "#ced0d5", 20);
        } else {
            String fileName = name.toLowerCase();
            String extension = getFileExtension(fileName);
            IconConfig iconConfig = extensionToIconMap.get(extension);

            if (iconConfig != null) {
                // Icon found in config
                return createIconFromConfig(iconConfig);
            } else {
                // Generic file icon in gray
                return createIcon(BootstrapIcons.FILE_EARMARK_CODE, "#c87d55", 20);
            }
        }
    }

    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(dotIndex + 1).toLowerCase();  // Remove the dot
        }
        return "";
    }

    public Node createIcon(Ikon iconCode, String color, int size) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(size);
        icon.setIconColor(javafx.scene.paint.Color.valueOf(color));
        return icon;
    }

    public Node createIconFromConfig(IconConfig iconConfig) {
        Ikon icon = getIconFromString(iconConfig.getIconClass());
        return createIcon(icon, iconConfig.getColor(), 20);
    }

    public Ikon getIconFromString(String iconClass) {
        try {
            if (iconClass.startsWith("Material.")) {
                return (Ikon) Enum.valueOf(Material.class, iconClass.substring(9));  // Remove "Material."
            } else if (iconClass.startsWith("BootstrapIcons.")) {
                return (Ikon) Enum.valueOf(BootstrapIcons.class, iconClass.substring(15));  // Remove "BootstrapIcons."
            } else if (iconClass.startsWith("Devicons.")) {
                return (Ikon) Enum.valueOf(Devicons.class, iconClass.substring(9));  // Remove "Devicons."
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

    public static class IconConfig {
        private String iconClass;
        private String color;

        public IconConfig(String iconClass, String color) {
            this.iconClass = iconClass;
            this.color = color;
        }

        public String getIconClass() {
            return iconClass;
        }

        public String getColor() {
            return color;
        }
    }
}
