package com.plotsquared.bukkit.chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

/**
 * A class containing static utility methods and caches which are intended as reflective conveniences.
 * Unless otherwise noted, upon failure methods will return {@code null}.
 */
public final class Reflection
{

    private static String _versionString;

    private Reflection()
    {

    }

    /**
     * Gets the version string from the package name of the CraftBukkit server implementation.
     * This is needed to bypass the JAR package name changing on each update.
     * @return The version string of the OBC and NMS packages, <em>including the trailing dot</em>.
     */
    public synchronized static String getVersion()
    {
        if (_versionString == null)
        {
            if (Bukkit.getServer() == null)
            {
                // The server hasn't started, static initializer call?
                return null;
            }
            final String name = Bukkit.getServer().getClass().getPackage().getName();
            _versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return _versionString;
    }

    /**
     * Stores loaded classes from the {@code net.minecraft.server} package.
     */
    private static final Map<String, Class<?>> _loadedNMSClasses = new HashMap<String, Class<?>>();
    /**
     * Stores loaded classes from the {@code org.bukkit.craftbukkit} package (and subpackages).
     */
    private static final Map<String, Class<?>> _loadedOBCClasses = new HashMap<String, Class<?>>();

    /**
     * Gets a {@link Class} object representing a type contained within the {@code net.minecraft.server} versioned package.
     * The class instances returned by this method are cached, such that no lookup will be done twice (unless multiple threads are accessing this method simultaneously).
     * @param className The name of the class, excluding the package, within NMS.
     * @return The class instance representing the specified NMS class, or {@code null} if it could not be loaded.
     */
    public synchronized static Class<?> getNMSClass(final String className)
    {
        if (_loadedNMSClasses.containsKey(className)) { return _loadedNMSClasses.get(className); }

        final String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(fullName);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            _loadedNMSClasses.put(className, null);
            return null;
        }
        _loadedNMSClasses.put(className, clazz);
        return clazz;
    }

    /**
     * Gets a {@link Class} object representing a type contained within the {@code org.bukkit.craftbukkit} versioned package.
     * The class instances returned by this method are cached, such that no lookup will be done twice (unless multiple threads are accessing this method simultaneously).
     * @param className The name of the class, excluding the package, within OBC. This name may contain a subpackage name, such as {@code inventory.CraftItemStack}.
     * @return The class instance representing the specified OBC class, or {@code null} if it could not be loaded.
     */
    public synchronized static Class<?> getOBCClass(final String className)
    {
        if (_loadedOBCClasses.containsKey(className)) { return _loadedOBCClasses.get(className); }

        final String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(fullName);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            _loadedOBCClasses.put(className, null);
            return null;
        }
        _loadedOBCClasses.put(className, clazz);
        return clazz;
    }

    /**
     * Attempts to get the NMS handle of a CraftBukkit object.
     * <p>
     * The only match currently attempted by this method is a retrieval by using a parameterless {@code getHandle()} method implemented by the runtime type of the specified object.
     * </p>
     * @param obj The object for which to retrieve an NMS handle.
     * @return The NMS handle of the specified object, or {@code null} if it could not be retrieved using {@code getHandle()}.
     */
    public synchronized static Object getHandle(final Object obj)
    {
        try
        {
            return getMethod(obj.getClass(), "getHandle").invoke(obj);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static final Map<Class<?>, Map<String, Field>> _loadedFields = new HashMap<Class<?>, Map<String, Field>>();

    /**
     * Retrieves a {@link Field} instance declared by the specified class with the specified name.
     * Java access modifiers are ignored during this retrieval. No guarantee is made as to whether the field
     * returned will be an instance or static field.
     * <p>
     * A global caching mechanism within this class is used to store fields. Combined with synchronization, this guarantees that
     * no field will be reflectively looked up twice.
     * </p>
     * <p>
     * If a field is deemed suitable for return, {@link Field#setAccessible(boolean) setAccessible} will be invoked with an argument of {@code true} before it is returned.
     * This ensures that callers do not have to check or worry about Java access modifiers when dealing with the returned instance.
     * </p>
     * @param clazz The class which contains the field to retrieve.
     * @param name The declared name of the field in the class.
     * @return A field object with the specified name declared by the specified class.
     * @see Class#getDeclaredField(String)
     */
    public synchronized static Field getField(final Class<?> clazz, final String name)
    {
        Map<String, Field> loaded;
        if (!_loadedFields.containsKey(clazz))
        {
            loaded = new HashMap<String, Field>();
            _loadedFields.put(clazz, loaded);
        }
        else
        {
            loaded = _loadedFields.get(clazz);
        }
        if (loaded.containsKey(name))
        {
            // If the field is loaded (or cached as not existing), return the relevant value, which might be null
            return loaded.get(name);
        }
        try
        {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            loaded.put(name, field);
            return field;
        }
        catch (final Exception e)
        {
            // Error loading
            e.printStackTrace();
            // Cache field as not existing
            loaded.put(name, null);
            return null;
        }
    }

    /**
     * Contains loaded methods in a cache.
     * The map maps [types to maps of [method names to maps of [parameter types to method instances]]].
     */
    private static final Map<Class<?>, Map<String, Map<ArrayWrapper<Class<?>>, Method>>> _loadedMethods = new HashMap<Class<?>, Map<String, Map<ArrayWrapper<Class<?>>, Method>>>();

    /**
     * Retrieves a {@link Method} instance declared by the specified class with the specified name and argument types.
     * Java access modifiers are ignored during this retrieval. No guarantee is made as to whether the field
     * returned will be an instance or static field.
     * <p>
     * A global caching mechanism within this class is used to store method. Combined with synchronization, this guarantees that
     * no method will be reflectively looked up twice.
     * </p>
     * <p>
     * If a method is deemed suitable for return, {@link Method#setAccessible(boolean) setAccessible} will be invoked with an argument of {@code true} before it is returned.
     * This ensures that callers do not have to check or worry about Java access modifiers when dealing with the returned instance.
     * </p>
     * <p>
     * This method does <em>not</em> search superclasses of the specified type for methods with the specified signature.
     * Callers wishing this behavior should use {@link Class#getDeclaredMethod(String, Class...)}.
     * @param clazz The class which contains the method to retrieve.
     * @param name The declared name of the method in the class.
     * @param args The formal argument types of the method.
     * @return A method object with the specified name declared by the specified class.
     */
    public synchronized static Method getMethod(final Class<?> clazz, final String name,
    final Class<?>... args)
    {
        if (!_loadedMethods.containsKey(clazz))
        {
            _loadedMethods.put(clazz, new HashMap<String, Map<ArrayWrapper<Class<?>>, Method>>());
        }

        final Map<String, Map<ArrayWrapper<Class<?>>, Method>> loadedMethodNames = _loadedMethods.get(clazz);
        if (!loadedMethodNames.containsKey(name))
        {
            loadedMethodNames.put(name, new HashMap<ArrayWrapper<Class<?>>, Method>());
        }

        final Map<ArrayWrapper<Class<?>>, Method> loadedSignatures = loadedMethodNames.get(name);
        final ArrayWrapper<Class<?>> wrappedArg = new ArrayWrapper<Class<?>>(args);
        if (loadedSignatures.containsKey(wrappedArg)) { return loadedSignatures.get(wrappedArg); }

        for (final Method m : clazz.getMethods())
        {
            if (m.getName().equals(name) && Arrays.equals(args, m.getParameterTypes()))
            {
                m.setAccessible(true);
                loadedSignatures.put(wrappedArg, m);
                return m;
            }
        }
        loadedSignatures.put(wrappedArg, null);
        return null;
    }

}
