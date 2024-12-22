/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration;

import com.plotsquared.core.configuration.Settings.Enabled_Components;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.util.StringMan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Config.class.getSimpleName());

    /**
     * Set the value of a specific node<br>
     * Probably throws some error if you supply non existing keys or invalid values
     *
     * @param key   config node
     * @param value value
     * @param root  configuration class
     */
    public static void set(String key, Object value, Class<? extends Config> root) {
        String[] split = key.split("\\.");
        Object instance = getInstance(split, root);
        if (instance != null) {
            Field field = getField(split, instance);
            if (field != null) {
                try {
                    if (field.getAnnotation(Final.class) != null) {
                        return;
                    }
                    if (field.getType() == String.class && !(value instanceof String)) {
                        value = value + "";
                    }
                    field.set(instance, value);
                    return;
                } catch (final Throwable e) {
                    LOGGER.error("Invalid configuration value '{}: {}' in {}", key, value, root.getSimpleName());
                    e.printStackTrace();
                }
            }
        }
        LOGGER.error("Failed to set config option '{}: {}' | {}", key, value, instance);
    }

    public static boolean load(File file, Class<? extends Config> root) {
        if (!file.exists()) {
            return false;
        }
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

    /**
     * Set all values in the file (load first to avoid overwriting)
     *
     * @param file file
     * @param root configuration file class
     */
    public static void save(File file, Class<? extends Config> root) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                Object instance = root.getDeclaredConstructor().newInstance();
                save(writer, root, instance, 0);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the static fields in a section.
     *
     * @param clazz config section
     * @return map or string against object of static fields
     */
    public static Map<String, Object> getFields(Class<Enabled_Components> clazz) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : clazz.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
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
                m.append(System.lineSeparator()).append(spacing).append("- ").append(toYamlString(obj, spacing));
            }
            return m.toString();
        }
        if (value instanceof String stringValue) {
            if (stringValue.isEmpty()) {
                return "''";
            }
            return "\"" + stringValue + "\"";
        }
        return value != null ? value.toString() : "null";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void save(PrintWriter writer, Class<?> clazz, Object instance, int indent) {
        try {
            String lineSeparator = System.lineSeparator();
            String spacing = StringMan.repeat(" ", indent);
            for (Field field : clazz.getFields()) {
                if (field.getAnnotation(Ignore.class) != null) {
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
                        for (Class<?> current : classes) {
                            if (StringMan.isEqual(current.getSimpleName(), field.getName())) {
                                field.set(instance, current.getDeclaredConstructor().newInstance());
                                break;
                            }
                        }
                    }
                } else {
                    writer.write(spacing + toNodeName(field.getName() + ": ") + toYamlString(
                            field.get(instance), spacing) + lineSeparator);
                }
            }
            for (Class<?> current : clazz.getClasses()) {
                if (current.isInterface() || current.getAnnotation(Ignore.class) != null) {
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
                    Field instanceField =
                            clazz.getDeclaredField(toFieldName(current.getSimpleName()));
                    setAccessible(instanceField);
                    ConfigBlock value = (ConfigBlock<?>) instanceField.get(instance);
                    if (value == null) {
                        value = new ConfigBlock();
                        instanceField.set(instance, value);
                        for (String blockName : blockNames.value()) {
                            value.put(blockName, current.getDeclaredConstructor().newInstance());
                        }
                    }
                    // Save each instance
                    for (Map.Entry<String, Object> entry : ((Map<String, Object>) value.getRaw())
                            .entrySet()) {
                        String key = entry.getKey();
                        writer.write(spacing + "  " + toNodeName(key) + ":" + lineSeparator);
                        save(writer, current, entry.getValue(), indent + 4);
                    }
                } else {
                    save(writer, current, current.getDeclaredConstructor().newInstance(), indent + 2);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the field for a specific config node and instance<br>
     * Note: As expiry can have multiple blocks there will be multiple instances
     *
     * @param split    the node (split by period)
     * @param instance the instance
     */
    private static Field getField(String[] split, Object instance) {
        try {
            Field field = instance.getClass().getField(toFieldName(split[split.length - 1]));
            setAccessible(field);
            return field;
        } catch (final Throwable e) {
            LOGGER.error("Invalid config field: {} for {}. It's likely you are in the process of updating from an older major " +
                            "release of PlotSquared. The entries named can be removed safely from the settings.yml. They are " +
                            "likely no longer in use, moved to a different location or have been merged with other " +
                            "configuration options. Check the changelog for more information.",
                    StringMan.join(split, "."), toNodeName(instance.getClass().getSimpleName())
            );
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the instance for a specific config node.
     *
     * @param split the node (split by period)
     * @param root
     * @return The instance or null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getInstance(String[] split, Class<?> root) {
        try {
            Class<?> clazz = root == null ? MethodHandles.lookup().lookupClass() : root;
            Object instance = clazz.getDeclaredConstructor().newInstance();
            while (split.length > 0) {
                if (split.length == 1) {
                    return instance;
                }
                Class<?> found = null;
                Class<?>[] classes = clazz.getDeclaredClasses();
                for (Class<?> current : classes) {
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
                            value = found.getDeclaredConstructor().newInstance();
                            instanceField.set(instance, value);
                        }
                        clazz = found;
                        instance = value;
                        split = Arrays.copyOfRange(split, 1, split.length);
                        continue;
                    }
                    ConfigBlock value = (ConfigBlock<?>) instanceField.get(instance);
                    if (value == null) {
                        value = new ConfigBlock();
                        instanceField.set(instance, value);
                    }
                    instance = value.get(split[1]);
                    if (instance == null) {
                        instance = found.getDeclaredConstructor().newInstance();
                        value.put(split[1], instance);
                    }
                    clazz = found;
                    split = Arrays.copyOfRange(split, 2, split.length);
                    continue;
                } catch (NoSuchFieldException ignore) {
                }
                if (found != null) {
                    split = Arrays.copyOfRange(split, 1, split.length);
                    clazz = found;
                    instance = clazz.getDeclaredConstructor().newInstance();
                    continue;
                }
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Translate a node to a java field name.
     *
     * @param node
     * @return
     */
    private static String toFieldName(String node) {
        return node.toUpperCase().replaceAll("-", "_");
    }

    /**
     * Translate a field to a config node.
     *
     * @param field
     * @return
     */
    private static String toNodeName(String field) {
        return field.toLowerCase().replace("_", "-");
    }

    /**
     * Set some field to be accessible.
     *
     * @param field
     */
    private static void setAccessible(Field field) {
        field.setAccessible(true);
    }

    /**
     * Indicates that a field should be instantiated / created.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Create {

    }


    /**
     * Indicates that a field cannot be modified.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Final {

    }


    /**
     * Creates a comment.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Documented
    public @interface Comment {

        String[] value();

    }


    /**
     * The names of any default blocks.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface BlockName {

        String[] value();

    }


    /**
     * Any field or class with is not part of the config.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Ignore {

    }


    @Ignore // This is not part of the config
    public static class ConfigBlock<T> {

        private final HashMap<String, T> INSTANCES = new HashMap<>();

        public T get(String key) {
            return INSTANCES.get(key);
        }

        public void put(String key, T value) {
            INSTANCES.put(key, value);
        }

        public Collection<T> getInstances() {
            return INSTANCES.values();
        }

        private Map<String, T> getRaw() {
            return INSTANCES;
        }

    }

}
