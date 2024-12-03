package com.example.textwrench.coremodules.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginManager {
    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
    private static PluginManager instance;
    private static final String PROPERTIES_FILE = "src/main/resources/plugin-states.properties";
    private Properties pluginStates = new Properties();

    private final List<TextWrenchPlugin> plugins = new ArrayList<>();
    private PluginContext pluginContext;

    private PluginManager() {}

    public static synchronized PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    /**
     * Dynamically load plugins from classpath and specified plugin directories
     * @param context Plugin context for initializing plugins
     */
    public void loadPlugins(PluginContext context) {
        this.pluginContext = context;

        try {
            // Use a more targeted approach to scanning
            scanClasspathSelectively();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading plugins", e);
        }
    }

    private void loadPluginStates() {
        try (FileInputStream in = new FileInputStream(PROPERTIES_FILE)) {
            pluginStates.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load plugin states", e);
        }
    }

    private void savePluginStates() {
        try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE)) {
            pluginStates.store(out, null);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not save plugin states", e);
        }
    }

    private void scanClasspathSelectively() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Get the current package to focus scanning
        String basePackage = "com.example.textwrench.plugins";

        // Use system classloader to avoid module-info issues
        Class<?>[] classes = getClasses(basePackage);

        for (Class<?> clazz : classes) {
            // Check if class implements TextWrenchPlugin
            if (TextWrenchPlugin.class.isAssignableFrom(clazz) &&
                    !clazz.isInterface() &&
                    !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {

                try {
                    // Create instance
                    TextWrenchPlugin plugin = (TextWrenchPlugin) clazz.getDeclaredConstructor().newInstance();

                    // Initialize plugin
                    plugin.initialize(pluginContext);

                    // Add if enabled
                    if (plugin.isEnabled()) {
                        plugins.add(plugin);
                        LOGGER.info("Loaded plugin: " + plugin.getPluginName());
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not instantiate plugin: " + clazz.getName(), e);
                }
            }
        }
    }

    /**
     * Utility method to get all classes in a package
     * @param packageName Package to scan
     * @return Array of classes in the package
     */
    private Class<?>[] getClasses(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<Class<?>> classes = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (resource.getProtocol().equals("file")) {
                classes.addAll(findClasses(new File(resource.toURI()), packageName));
            }
        }

        return classes.toArray(new Class[0]);
    }

    /**
     * Recursive method to find classes in a directory
     * @param directory Directory to scan
     * @param packageName Current package name
     * @return List of classes found
     */
    private List<Class<?>> findClasses(File directory, String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                Class<?> cls = Class.forName(packageName + "." +
                        file.getName().substring(0, file.getName().length() - 6));
                classes.add(cls);
            }
        }

        return classes;
    }


    /**
     * Scan a directory for plugin classes
     * @param directory Directory to scan
     * @param packageName Current package name
     */
    private void findPluginsInDirectory(File directory, String packageName) {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively scan subdirectories
                findPluginsInDirectory(file,
                        packageName + (packageName.isEmpty() ? "" : ".") + file.getName());
            } else if (file.getName().endsWith(".class")) {
                // Check if class is a plugin
                String className = packageName + "." +
                        file.getName().substring(0, file.getName().length() - 6);

                tryLoadPlugin(className);
            }
        }
    }

    /**
     * Attempt to load and initialize a plugin class
     * @param className Fully qualified class name
     */
    private void tryLoadPlugin(String className) {
        try {
            Class<?> clazz = Class.forName(className);

            // Check if class implements TextWrenchPlugin
            if (TextWrenchPlugin.class.isAssignableFrom(clazz) &&
                    !clazz.isInterface() &&
                    !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {

                // Create instance
                TextWrenchPlugin plugin = (TextWrenchPlugin) clazz.getDeclaredConstructor().newInstance();

                // Initialize plugin
                plugin.initialize(pluginContext);

                String pluginId = plugin.getPluginId();
                boolean enabled = Boolean.parseBoolean(pluginStates.getProperty(pluginId, "true"));
                plugin.setEnabled(enabled);

                // Add if enabled
                if (plugin.isEnabled()) {
                    plugins.add(plugin);
                    LOGGER.info("Loaded plugin: " + plugin.getPluginName());
                }
            }
        } catch (Exception e) {
            // Silently ignore classes that can't be instantiated
            LOGGER.log(Level.FINE, "Could not load plugin class: " + className, e);
        }
    }


    // Existing methods remain the same
    public List<TextWrenchPlugin> getPlugins() {
        return new ArrayList<>(plugins);
    }

    public TextWrenchPlugin getPluginById(String pluginId) {
        return plugins.stream()
                .filter(p -> p.getPluginId().equals(pluginId))
                .findFirst()
                .orElse(null);
    }

    public void setPluginEnabled(String pluginId, boolean enabled) {
        TextWrenchPlugin plugin = getPluginById(pluginId);
        if (plugin != null) {
            plugin.setEnabled(enabled);
            pluginStates.setProperty(pluginId, Boolean.toString(enabled));
            savePluginStates();
        }
    }
}