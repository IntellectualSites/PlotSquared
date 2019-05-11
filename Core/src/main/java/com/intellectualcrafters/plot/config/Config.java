package com.intellectualcrafters.plot.config;

import com.intellectualcrafters.configuration.MemorySection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.util.StringMan;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Config {

    /**
     * Get the value for a node<br>
     *     Probably throws some error if you try to get a non existent key
     * @param key
     * @param root
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Class root) {
        String[] split = key.split("\\.");
        Object instance = getInstance(split, root);
        if (instance != null) {
            Field field = getField(split, instance);
            if (field != null) {
                try {
                    return (T) field.get(instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        PS.debug("Failed to get config option: " + key);
        return null;
    }

    /**
     * Set the value of a specific node<br>
     *     Probably throws some error if you supply non existing keys or invalid values
     * @param key config node
     * @param value value
     * @param root
     *
     */
    public static void set(String key, Object value, Class root) {
        String[] split = key.split(".");
        Object instance = getInstance(split, root);
        if (instance != null) {
            Field field = getField(split, instance);
            if (field != null) {
                try {
                    if (field.isAnnotationPresent(Final.class)) {
                        return;
                    }
                    if (field.getType() == String.class && !(value instanceof String)) {
                        value = value + "";
                    }
                    field.set(instance, value);
                    return;
                } catch (Throwable e) {
                    PS.debug("Invalid configuration value: " + key + ": " + value + " in " + root.getSimpleName());
                    e.printStackTrace();
                }
            }
        }
        PS.debug("Failed to set config option: " + key + ": " + value + " | " + instance);
    }

    /**
     * Loads a file based on the file name and configuration class.
     * @param file the file to load
     * @param root the class to base the configuration information off of.
     * @return true if the file exists
     */
    public static boolean load(File file, Class root) {
        checkNotNull(root, "Root was null. This shouldn't happen.");
        if (file.exists()) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            for (String key : yml.getKeys(true)) {
                Object value = yml.get(key);
                if (value instanceof MemorySection) {
                    continue;
                }
                set(key, value, root);
            }
            return true;
        }
        return false;
    }

    /**
     * Set all values in the file (load first to avoid overwriting)
     * @param file
     * @param root
     */
    public static void save(File file, Class root) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                Object instance = root.newInstance();
                save(writer, root, instance, 0);
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the static fields in a section.
     * @param clazz
     * @return
     */
    public static Map<String, Object> getFields(Class clazz) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    map.put(toNodeName(field.getName()), field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    private static String toYamlString(Object value, String spacing) {
        if (value instanceof List) {
            Collection<?> listValue = (Collection<?>) value;
            if (listValue.isEmpty()) {
                return "[]";
            }
            StringBuilder m = new StringBuilder();
            for (Object obj : listValue) {
                m.append(System.lineSeparator() + spacing + "- " + toYamlString(obj, spacing));
            }
            return m.toString();
        }
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.isEmpty()) {
                return "''";
            }
            return "\"" + stringValue + "\"";
        }
        return value != null ? value.toString() : "null";
    }

    private static void save(PrintWriter writer, Class clazz, Object instance, int indent) {
        try {
            String lineSeparator = System.lineSeparator();
            String spacing = StringMan.repeat(" ", indent);
            for (Field field : clazz.getFields()) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }
                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) {
                    for (String commentLine : comment.value()) {
                        writer.write(spacing + "# " + commentLine + lineSeparator);
                    }
                }
                Create create = field.getAnnotation(Create.class);
                if (create != null) {
                    Object value = field.get(instance);
                    if (value == null && field.getType() != ConfigBlock.class) {
                        setAccessible(field);
                        Class<?>[] classes = clazz.getDeclaredClasses();
                        for (Class current : classes) {
                            if (StringMan.isEqual(current.getSimpleName(), field.getName())) {
                                field.set(instance, current.newInstance());
                                break;
                            }
                        }
                    }
                    continue;
                } else {
                    writer.write(spacing + toNodeName(field.getName() + ": ") + toYamlString(field.get(instance), spacing) + lineSeparator);
                }
            }
            for (Class<?> current : clazz.getClasses()) {
                if (current.isInterface() || current.isAnnotationPresent(Ignore.class)) {
                    continue;
                }
                if (indent == 0) {
                    writer.write(lineSeparator);
                }
                Comment comment = current.getAnnotation(Comment.class);
                if (comment != null) {
                    for (String commentLine : comment.value()) {
                        writer.write(spacing + "# " + commentLine + lineSeparator);
                    }
                }
                writer.write(spacing + toNodeName(current.getSimpleName()) + ":" + lineSeparator);
                BlockName blockNames = current.getAnnotation(BlockName.class);
                if (blockNames != null) {
                    Field instanceField = clazz.getDeclaredField(toFieldName(current.getSimpleName()));
                    setAccessible(instanceField);
                    ConfigBlock value = (ConfigBlock) instanceField.get(instance);
                    if (value == null) {
                        value = new ConfigBlock();
                        instanceField.set(instance, value);
                        for (String blockName : blockNames.value()) {
                            value.put(blockName, current.newInstance());
                        }
                    }
                    // Save each instance
                    for (Map.Entry<String, Object> entry: ((Map<String, Object>) value.getRaw()).entrySet()) {
                        String key = entry.getKey();
                        writer.write(spacing + "  " + toNodeName(key) + ":" + lineSeparator);
                        save(writer, current, entry.getValue(), indent + 4);
                    }
                    continue;
                } else {
                    save(writer, current, current.newInstance(), indent + 2);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the field for a specific config node.
     * @param split the node (split by period)
     * @param root
     * @return
     */
    private static Field getField(String[] split, Class root) {
        Object instance = getInstance(split, root);
        if (instance == null) {
            return null;
        }
        return getField(split, instance);
    }

    /**
     * Get the field for a specific config node and instance<br>
     *     Note: As expiry can have multiple blocks there will be multiple instances
     * @param split the node (split by period)
     * @param instance the instance
     * @return
     */
    private static Field getField(String[] split, Object instance) {
        try {
            Field field = instance.getClass().getField(toFieldName(split[split.length - 1]));
            setAccessible(field);
            return field;
        } catch (Throwable e) {
            PS.debug("Invalid config field: " + StringMan.join(split, ".") + " for " + toNodeName(instance.getClass().getSimpleName()));
            return null;
        }
    }

    /**
     * Get the instance for a specific config node.
     * @param split the node (split by period)
     * @param root
     * @return The instance or null
     */
    private static Object getInstance(String[] split, Class root) {
        try {
            Class<?> clazz;
            if (root == null) {
                clazz = MethodHandles.lookup().lookupClass();
            } else {
                clazz = root;
            }
            Object instance = clazz.newInstance();
            while (split.length > 0) {
                switch (split.length) {
                    case 1:
                        return instance;
                    default:
                        Class found = null;
                        Class<?>[] classes = clazz.getDeclaredClasses();
                        for (Class current : classes) {
                            if (current.getSimpleName().equalsIgnoreCase(toFieldName(split[0]))) {
                                found = current;
                                break;
                            }
                        }
                        try {
                            Field instanceField = clazz.getDeclaredField(toFieldName(split[0]));
                            setAccessible(instanceField);
                            if (instanceField.getType() != ConfigBlock.class) {
                                Object value = instanceField.get(instance);
                                if (value == null) {
                                    value = found.newInstance();
                                    instanceField.set(instance, value);
                                }
                                clazz = found;
                                instance = value;
                                split = Arrays.copyOfRange(split, 1, split.length);
                                continue;
                            }
                            ConfigBlock value = (ConfigBlock) instanceField.get(instance);
                            if (value == null) {
                                value = new ConfigBlock();
                                instanceField.set(instance, value);
                            }
                            instance = value.get(split[1]);
                            if (instance == null) {
                                instance = found.newInstance();
                                value.put(split[1], instance);
                            }
                            clazz = found;
                            split = Arrays.copyOfRange(split, 2, split.length);
                            continue;
                        } catch (NoSuchFieldException ignore) { }
                        if (found != null) {
                            split = Arrays.copyOfRange(split, 1, split.length);
                            clazz = found;
                            instance = clazz.newInstance();
                            continue;
                        }
                        return null;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Translate a node to a java field name.
     * @param node
     * @return
     */
    private static String toFieldName(String node) {
        return node.toUpperCase().replaceAll("-","_");
    }

    /**
     * Translate a field to a config node.
     * @param field
     * @return
     */
    private static String toNodeName(String field) {
        return field.toLowerCase().replace("_","-");
    }

    /**
     * Set some field to be accessible.
     * @param field
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setAccessible(Field field) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    /**
     * Indicates that a field should be instantiated / created.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public  @interface Create {}

    /**
     * Indicates that a field cannot be modified.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Final {}

    /**
     * Creates a comment.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.TYPE})
    public @interface Comment {
        String[] value();
    }

    /**
     * The names of any default blocks.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.TYPE})
    public @interface BlockName {
        String[] value();
    }

    /**
     * Any field or class with is not part of the config.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.TYPE})
    public @interface Ignore {}

    @Ignore // This is not part of the config
    public static class ConfigBlock<T> {

        private HashMap<String, T> INSTANCES = new HashMap<>();

        public T get(String key) {
            return INSTANCES.get(key);
        }

        public void put(String key, T value) {
            INSTANCES.put(key, value);
        }

        public Collection<T> getInstances() {
            return INSTANCES.values();
        }

        public Collection<String> getSections() {
            return INSTANCES.keySet();
        }

        private Map<String, T> getRaw() {
            return INSTANCES;
        }
    }
}
