package com.example.textwrench.coremodules.service;

import com.example.textwrench.coremodules.model.ProjectItem;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class ProjectService {
    public static ProjectItem loadProject(File rootDirectory) {
        ProjectItem rootItem = new ProjectItem(rootDirectory);
        loadChildren(rootItem);
        return rootItem;
    }

    private static void loadChildren(ProjectItem parentItem) {
        File[] files = parentItem.getFile().listFiles();
        if (files != null) {
            // Sort files to have folders first, then alphabetically
            Arrays.sort(files, Comparator.comparingInt(f -> f.isDirectory() ? 0 : 1));

            for (File file : files) {
                // Ignore hidden files and directories
                if (!file.isHidden()) {
                    ProjectItem childItem = new ProjectItem(file);
                    parentItem.addChild(childItem);

                    // Recursively load children for directories
                    if (file.isDirectory()) {
                        loadChildren(childItem);
                    }
                }
            }
        }
    }
}