package com.plotsquared.bukkit.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection Utilities for minecraft
 *
 */
public class ReflectionUtil {
    public static Class<?> getNmsClass(final String name) {
        final String className = "net.minecraft.server." + getVersion() + "." + name;
        return getClass(className);
    }

    public static Class<?> getCbClass(final String name) {
        final String className = "org.bukkit.craftbukkit." + getVersion() + "." + name;
        return getClass(className);
    }

    public static Class<?> getUtilClass(final String name) {
        try {
            return Class.forName(name); //Try before 1.8 first
        } catch (final ClassNotFoundException ex) {
            try {
                return Class.forName("net.minecraft.util." + name); //Not 1.8
            } catch (final ClassNotFoundException ex2) {
                return null;
            }
        }
    }

    public static String getVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static Object getHandle(final Object wrapper) {
        final Method getHandle = makeMethod(wrapper.getClass(), "getHandle");
        return callMethod(getHandle, wrapper);
    }

    //Utils
    public static Method makeMethod(final Class<?> clazz, final String methodName, final Class<?>... paramaters) {
        try {
            return clazz.getDeclaredMethod(methodName, paramaters);
        } catch (final NoSuchMethodException ex) {
            return null;
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(final Method method, final Object instance, final Object... paramaters) {
        if (method == null) {
            throw new RuntimeException("No such method");
        }
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, paramaters);
        } catch (final InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> makeConstructor(final Class<?> clazz, final Class<?>... paramaterTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(paramaterTypes);
        } catch (final NoSuchMethodException ex) {
            return null;
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T callConstructor(final Constructor<T> constructor, final Object... paramaters) {
        if (constructor == null) {
            throw new RuntimeException("No such constructor");
        }
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(paramaters);
        } catch (final InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field makeField(final Class<?> clazz, final String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (final NoSuchFieldException ex) {
            return null;
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(final Field field, final Object instance) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            return (T) field.get(instance);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setField(final Field field, final Object instance, final Object value) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Class<?> getClass(final String name) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException ex) {
            return null;
        }
    }

    public static <T> Class<? extends T> getClass(final String name, final Class<T> superClass) {
        try {
            return Class.forName(name).asSubclass(superClass);
        } catch (ClassCastException | ClassNotFoundException ex) {
            return null;
        }
    }
}
