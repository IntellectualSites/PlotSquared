////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author DPOH-VAR
 * @version 1.0
 */
public class ReflectionUtils {
    
    private static String version;

    public ReflectionUtils(final String version) {
        ReflectionUtils.version = version;
        preClassB += "." + version;
        preClassM += "." + version;
    }
    
    /**
     * prefix of bukkit classes
     */
    private static String preClassB = "org.bukkit.craftbukkit";
    /**
     * prefix of minecraft classes
     */
    private static String preClassM = "net.minecraft.server";
    
    public static Class<?> getNmsClass(final String name) {
        final String className = "net.minecraft.server." + version + "." + name;
        return getClass(className);
    }
    
    public static Class<?> getCbClass(final String name) {
        final String className = "org.bukkit.craftbukkit." + version + "." + name;
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
    
    /**
     * Get class for name. Replace {nms} to net.minecraft.server.V*. Replace {cb} to org.bukkit.craftbukkit.V*. Replace
     * {nm} to net.minecraft
     *
     * @param classes possible class paths
     *
     * @return RefClass object
     *
     * @throws RuntimeException if no class found
     */
    public static RefClass getRefClass(final String... classes) throws RuntimeException {
        for (String className : classes) {
            try {
                className = className.replace("{cb}", preClassB).replace("{nms}", preClassM).replace("{nm}", "net.minecraft");
                return getRefClass(Class.forName(className));
            } catch (final ClassNotFoundException ignored) {}
        }
        throw new RuntimeException("no class found");
    }
    
    /**
     * get RefClass object by real class
     *
     * @param clazz class
     *
     * @return RefClass based on passed class
     */
    public static RefClass getRefClass(final Class clazz) {
        return new RefClass(clazz);
    }
    
    /**
     * RefClass - utility to simplify work with reflections.
     */
    public static class RefClass {
        private final Class<?> clazz;
        
        private RefClass(final Class<?> clazz) {
            this.clazz = clazz;
        }
        
        /**
         * get passed class
         *
         * @return class
         */
        public Class<?> getRealClass() {
            return clazz;
        }
        
        /**
         * see {@link Class#isInstance(Object)}
         *
         * @param object the object to check
         *
         * @return true if object is an instance of this class
         */
        public boolean isInstance(final Object object) {
            return clazz.isInstance(object);
        }
        
        /**
         * get existing method by name and types
         *
         * @param name  name
         * @param types method parameters. can be Class or RefClass
         *
         * @return RefMethod object
         *
         * @throws RuntimeException if method not found
         */
        public RefMethod getMethod(final String name, final Object... types) throws NoSuchMethodException {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (final Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    } else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    } else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefMethod(clazz.getMethod(name, classes));
                } catch (final NoSuchMethodException ignored) {
                    return new RefMethod(clazz.getDeclaredMethod(name, classes));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * get existing constructor by types
         *
         * @param types parameters. can be Class or RefClass
         *
         * @return RefMethod object
         *
         * @throws RuntimeException if constructor not found
         */
        public RefConstructor getConstructor(final Object... types) {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (final Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    } else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    } else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefConstructor(clazz.getConstructor(classes));
                } catch (final NoSuchMethodException ignored) {
                    return new RefConstructor(clazz.getDeclaredConstructor(classes));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * find method by type parameters
         *
         * @param types parameters. can be Class or RefClass
         *
         * @return RefMethod object
         *
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethod(final Object... types) {
            final Class[] classes = new Class[types.length];
            int t = 0;
            for (final Object e : types) {
                if (e instanceof Class) {
                    classes[t++] = (Class) e;
                } else if (e instanceof RefClass) {
                    classes[t++] = ((RefClass) e).getRealClass();
                } else {
                    classes[t++] = e.getClass();
                }
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, clazz.getMethods());
            Collections.addAll(methods, clazz.getDeclaredMethods());
            findMethod: for (final Method m : methods) {
                final Class<?>[] methodTypes = m.getParameterTypes();
                if (methodTypes.length != classes.length) {
                    continue;
                }
                for (final Class aClass : classes) {
                    if (!Arrays.equals(classes, methodTypes)) {
                        continue findMethod;
                    }
                    return new RefMethod(m);
                }
            }
            throw new RuntimeException("no such method");
        }
        
        /**
         * find method by name
         *
         * @param names possible names of method
         *
         * @return RefMethod object
         *
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByName(final String... names) {
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, clazz.getMethods());
            Collections.addAll(methods, clazz.getDeclaredMethods());
            for (final Method m : methods) {
                for (final String name : names) {
                    if (m.getName().equals(name)) {
                        return new RefMethod(m);
                    }
                }
            }
            throw new RuntimeException("no such method");
        }
        
        /**
         * find method by return value
         *
         * @param type type of returned value
         *
         * @return RefMethod
         *
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByReturnType(final RefClass type) {
            return findMethodByReturnType(type.clazz);
        }
        
        /**
         * find method by return value
         *
         * @param type type of returned value
         *
         * @return RefMethod
         *
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByReturnType(Class type) {
            if (type == null) {
                type = void.class;
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, clazz.getMethods());
            Collections.addAll(methods, clazz.getDeclaredMethods());
            for (final Method m : methods) {
                if (type.equals(m.getReturnType())) {
                    return new RefMethod(m);
                }
            }
            throw new RuntimeException("no such method");
        }
        
        /**
         * find constructor by number of arguments
         *
         * @param number number of arguments
         *
         * @return RefConstructor
         *
         * @throws RuntimeException if constructor not found
         */
        public RefConstructor findConstructor(final int number) {
            final List<Constructor> constructors = new ArrayList<>();
            Collections.addAll(constructors, clazz.getConstructors());
            Collections.addAll(constructors, clazz.getDeclaredConstructors());
            for (final Constructor m : constructors) {
                if (m.getParameterTypes().length == number) {
                    return new RefConstructor(m);
                }
            }
            throw new RuntimeException("no such constructor");
        }
        
        /**
         * get field by name
         *
         * @param name field name
         *
         * @return RefField
         *
         * @throws RuntimeException if field not found
         */
        public RefField getField(final String name) {
            try {
                try {
                    return new RefField(clazz.getField(name));
                } catch (final NoSuchFieldException ignored) {
                    return new RefField(clazz.getDeclaredField(name));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * find field by type
         *
         * @param type field type
         *
         * @return RefField
         *
         * @throws RuntimeException if field not found
         */
        public RefField findField(final RefClass type) {
            return findField(type.clazz);
        }
        
        /**
         * find field by type
         *
         * @param type field type
         *
         * @return RefField
         *
         * @throws RuntimeException if field not found
         */
        public RefField findField(Class type) {
            if (type == null) {
                type = void.class;
            }
            final List<Field> fields = new ArrayList<>();
            Collections.addAll(fields, clazz.getFields());
            Collections.addAll(fields, clazz.getDeclaredFields());
            for (final Field f : fields) {
                if (type.equals(f.getType())) {
                    return new RefField(f);
                }
            }
            throw new RuntimeException("no such field");
        }
    }
    
    /**
     * Method wrapper
     */
    public static class RefMethod {
        private final Method method;
        
        private RefMethod(final Method method) {
            this.method = method;
            method.setAccessible(true);
        }
        
        /**
         * @return passed method
         */
        public Method getRealMethod() {
            return method;
        }
        
        /**
         * @return owner class of method
         */
        public RefClass getRefClass() {
            return new RefClass(method.getDeclaringClass());
        }
        
        /**
         * @return class of method return type
         */
        public RefClass getReturnRefClass() {
            return new RefClass(method.getReturnType());
        }
        
        /**
         * apply method to object
         *
         * @param e object to which the method is applied
         *
         * @return RefExecutor with method call(...)
         */
        public RefExecutor of(final Object e) {
            return new RefExecutor(e);
        }
        
        /**
         * call static method
         *
         * @param params sent parameters
         *
         * @return return value
         */
        public Object call(final Object... params) {
            try {
                return method.invoke(null, params);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public class RefExecutor {
            final Object e;
            
            public RefExecutor(final Object e) {
                this.e = e;
            }
            
            /**
             * apply method for selected object
             *
             * @param params sent parameters
             *
             * @return return value
             *
             * @throws RuntimeException if something went wrong
             */
            public Object call(final Object... params) {
                try {
                    return method.invoke(e, params);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    /**
     * Constructor wrapper
     */
    public static class RefConstructor {
        private final Constructor constructor;
        
        private RefConstructor(final Constructor constructor) {
            this.constructor = constructor;
            constructor.setAccessible(true);
        }
        
        /**
         * @return passed constructor
         */
        public Constructor getRealConstructor() {
            return constructor;
        }
        
        /**
         * @return owner class of method
         */
        public RefClass getRefClass() {
            return new RefClass(constructor.getDeclaringClass());
        }
        
        /**
         * create new instance with constructor
         *
         * @param params parameters for constructor
         *
         * @return new object
         *
         * @throws RuntimeException if something went wrong
         */
        public Object create(final Object... params) {
            try {
                return constructor.newInstance(params);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static class RefField {
        private final Field field;
        
        private RefField(final Field field) {
            this.field = field;
            field.setAccessible(true);
        }
        
        /**
         * @return passed field
         */
        public Field getRealField() {
            return field;
        }
        
        /**
         * @return owner class of field
         */
        public RefClass getRefClass() {
            return new RefClass(field.getDeclaringClass());
        }
        
        /**
         * @return type of field
         */
        public RefClass getFieldRefClass() {
            return new RefClass(field.getType());
        }
        
        /**
         * apply fiend for object
         *
         * @param e applied object
         *
         * @return RefExecutor with getter and setter
         */
        public RefExecutor of(final Object e) {
            return new RefExecutor(e);
        }
        
        public class RefExecutor {
            final Object e;
            
            public RefExecutor(final Object e) {
                this.e = e;
            }
            
            /**
             * set field value for applied object
             *
             * @param param value
             */
            public void set(final Object param) {
                try {
                    field.set(e, param);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            /**
             * get field value for applied object
             *
             * @return value of field
             */
            public Object get() {
                try {
                    return field.get(e);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
