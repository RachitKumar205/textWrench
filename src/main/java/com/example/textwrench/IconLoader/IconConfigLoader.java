package com.example.textwrench.IconLoader;

import com.example.textwrench.coremodules.model.ProjectItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.devicons.Devicons;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IconConfigLoader {

    private static Map<String, ProjectItem.IconConfig> extensionToIconMap;

    public static Map<String, ProjectItem.IconConfig> loadIconConfiguration() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream configStream = ProjectItem.class.getClassLoader().getResourceAsStream("language-icons.json");

            if (configStream == null) {
                throw new IOException("Config file not found in resources folder.");
            }

            JsonNode rootNode = objectMapper.readTree(configStream);

            extensionToIconMap = new HashMap<>();


            rootNode.fieldNames().forEachRemaining(language -> {
                JsonNode languageNode = rootNode.get(language);
                String iconClass = languageNode.get("class").asText();
                String color = languageNode.get("color").asText();
                extensionToIconMap.put(language, new ProjectItem.IconConfig(iconClass, color));
            });

        } catch (IOException e) {
            e.printStackTrace();
            extensionToIconMap = new HashMap<>(); // In case of error, use an empty map
        }
        return extensionToIconMap;
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

    public Node determineFileIcon(String name) {
            String fileName = name.toLowerCase();
            String extension = getFileExtension(fileName);
            ProjectItem.IconConfig iconConfig = extensionToIconMap.get(extension);

            if (iconConfig != null) {
                // Icon found in config
                return createIconFromConfig(iconConfig);
            } else {
                // Generic file icon in gray
                return createIcon(BootstrapIcons.FILE_EARMARK_CODE, "#c87d55", 16);
            }
    }

    public Node createIconFromConfig(ProjectItem.IconConfig iconConfig) {
        Ikon icon = getIconFromString(iconConfig.getIconClass());
        return createIcon(icon, iconConfig.getColor(), 14);
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
}
