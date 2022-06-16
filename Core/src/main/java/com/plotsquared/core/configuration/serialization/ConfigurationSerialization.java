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
package com.plotsquared.core.configuration.serialization;

import com.plotsquared.core.configuration.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for storing and retrieving classes for {@link Configuration}.
 */
public class ConfigurationSerialization {

    public static final String SERIALIZED_TYPE_KEY = "==";
    private static final Map<String, Class<? extends ConfigurationSerializable>> aliases =
            new HashMap<>();
    private final Class<? extends ConfigurationSerializable> clazz;

    protected ConfigurationSerialization(Class<? extends ConfigurationSerializable> clazz) {
        this.clazz = clazz;
    }

    /**
     * Attempts to deserialize the given arguments into a new instance of the
     * given class.
     * <p>The class must implement {@link ConfigurationSerializable}, including
     * the extra methods as specified in the javadoc of
     * ConfigurationSerializable.</p>
     * <p>If a new instance could not be made, an example being the class not
     * fully implementing the interface, null will be returned.</p>
     *
     * @param args  Arguments for deserialization
     * @param clazz Class to deserialize into
     * @return New instance of the specified class
     */
    public static ConfigurationSerializable deserializeObject(
            Map<String, ?> args,
            Class<? extends ConfigurationSerializable> clazz
    ) {
        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    /**
     * Attempts to deserialize the given arguments into a new instance of the
     * given class.
     *
     * <p>The class must implement {@link ConfigurationSerializable}, including
     * the extra methods as specified in the javadoc of
     * ConfigurationSerializable.</p>
     * <p>If a new instance could not be made, an example being the class not
     * fully implementing the interface, null will be returned.</p>
     *
     * @param args Arguments for deserialization
     * @return New instance of the specified class
     */
    public static ConfigurationSerializable deserializeObject(Map<String, ?> args) {
        Class<? extends ConfigurationSerializable> clazz = null;

        if (args.containsKey(SERIALIZED_TYPE_KEY)) {
            try {
                String alias = (String) args.get(SERIALIZED_TYPE_KEY);

                if (alias == null) {
                    throw new IllegalArgumentException("Cannot have null alias");
                }
                clazz = getClassByAlias(alias);
                if (clazz == null) {
                    throw new IllegalArgumentException(
                            "Specified class does not exist ('" + alias + "')");
                }
            } catch (ClassCastException ex) {
                ex.fillInStackTrace();
                throw ex;
            }
        } else {
            throw new IllegalArgumentException(
                    "Args doesn't contain type key ('" + SERIALIZED_TYPE_KEY + "')");
        }

        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    /**
     * Registers the given {@link ConfigurationSerializable} class by its
     * alias.
     *
     * @param clazz Class to register
     */
    public static void registerClass(Class<? extends ConfigurationSerializable> clazz) {
        DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);

        if (delegate == null) {
            registerClass(clazz, getAlias(clazz));
            registerClass(clazz, clazz.getName());
        }
    }

    /**
     * Registers the given alias to the specified {@link
     * ConfigurationSerializable} class.
     *
     * @param clazz Class to register
     * @param alias Alias to register as
     * @see SerializableAs
     */
    public static void registerClass(
            Class<? extends ConfigurationSerializable> clazz,
            String alias
    ) {
        aliases.put(alias, clazz);
    }

    /**
     * Unregisters the specified alias to a {@link ConfigurationSerializable}
     *
     * @param alias Alias to unregister
     */
    public static void unregisterClass(String alias) {
        aliases.remove(alias);
    }

    /**
     * Unregisters any aliases for the specified {@link
     * ConfigurationSerializable} class.
     *
     * @param clazz Class to unregister
     */
    public static void unregisterClass(Class<? extends ConfigurationSerializable> clazz) {
        while (aliases.values().remove(clazz)) {
        }
    }

    /**
     * Attempts to get a registered {@link ConfigurationSerializable} class by
     * its alias.
     *
     * @param alias Alias of the serializable
     * @return Registered class, or null if not found
     */
    public static Class<? extends ConfigurationSerializable> getClassByAlias(String alias) {
        return aliases.get(alias);
    }

    /**
     * Gets the correct alias for the given {@link ConfigurationSerializable}
     * class.
     *
     * @param clazz Class to get alias for
     * @return Alias to use for the class
     */
    public static String getAlias(Class<? extends ConfigurationSerializable> clazz) {
        DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);

        if (delegate != null) {
            if (delegate.value() == clazz) {
                delegate = null;
            } else {
                return getAlias(delegate.value());
            }
        }

        SerializableAs alias = clazz.getAnnotation(SerializableAs.class);

        if (alias != null) {
            return alias.value();
        }

        return clazz.getName();
    }

    protected Method getMethod(String name, boolean isStatic) {
        try {
            Method method = this.clazz.getDeclaredMethod(name, Map.class);

            if (!ConfigurationSerializable.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            if (Modifier.isStatic(method.getModifiers()) != isStatic) {
                return null;
            }

            return method;
        } catch (NoSuchMethodException | SecurityException ignored) {
            return null;
        }
    }

    protected Constructor<? extends ConfigurationSerializable> getConstructor() {
        try {
            return this.clazz.getConstructor(Map.class);
        } catch (NoSuchMethodException | SecurityException ignored) {
            return null;
        }
    }

    protected ConfigurationSerializable deserializeViaMethod(Method method, Map<String, ?> args) {
        try {
            ConfigurationSerializable result =
                    (ConfigurationSerializable) method.invoke(null, args);

            if (result == null) {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(
                        Level.SEVERE,
                        "Could not call method '" + method + "' of " + this.clazz
                                + " for deserialization: method returned null"
                );
            } else {
                return result;
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ex) {
            if (ex instanceof InvocationTargetException) {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                        "Could not call method '" + method + "' of " + this.clazz
                                + " for deserialization", ex.getCause()
                );
            } else {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                        "Could not call method '" + method + "' of " + this.clazz
                                + " for deserialization", ex
                );
            }
        }

        return null;
    }

    protected ConfigurationSerializable deserializeViaCtor(
            Constructor<? extends ConfigurationSerializable> ctor, Map<String, ?> args
    ) {
        try {
            return ctor.newInstance(args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            if (ex instanceof InvocationTargetException) {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                        "Could not call constructor '" + ctor + "' of " + this.clazz
                                + " for deserialization", ex.getCause()
                );
            } else {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                        "Could not call constructor '" + ctor + "' of " + this.clazz
                                + " for deserialization", ex
                );
            }
        }

        return null;
    }

    public ConfigurationSerializable deserialize(Map<String, ?> args) {
        if (args == null) {
            throw new NullPointerException("Args must not be null");
        }
        ConfigurationSerializable result = null;
        Method method = getMethod("deserialize", true);
        if (method != null) {
            result = deserializeViaMethod(method, args);
        }
        if (result == null) {
            method = getMethod("valueOf", true);
            if (method != null) {
                result = deserializeViaMethod(method, args);
            }
        }
        if (result == null) {
            Constructor<? extends ConfigurationSerializable> constructor = getConstructor();
            if (constructor != null) {
                result = deserializeViaCtor(constructor, args);
            }
        }

        return result;
    }

}
