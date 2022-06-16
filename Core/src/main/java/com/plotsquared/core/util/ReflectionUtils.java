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
package com.plotsquared.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author DPOH-VAR
 * @version 1.0
 */
public class ReflectionUtils {

    /**
     * prefix of bukkit classes
     */
    private static String preClassB = "org.bukkit.craftbukkit";
    /**
     * prefix of minecraft classes
     */
    private static String preClassM = "net.minecraft.server";

    public ReflectionUtils(String version) {
        if (version != null) {
            preClassB += '.' + version;
            preClassM += '.' + version;
        }
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static <T> Class<? extends T> getClass(String name, Class<T> superClass) {
        try {
            return Class.forName(name).asSubclass(superClass);
        } catch (ClassCastException | ClassNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Get class for name. Replace {nms} to net.minecraft.server.V*. Replace {cb} to org.bukkit.craftbukkit.V*. Replace
     * {nm} to net.minecraft
     *
     * @param className possible class paths
     * @return RefClass object
     * @throws ClassNotFoundException if no class found
     */
    public static RefClass getRefClass(String className) throws ClassNotFoundException {
        className = className.replace("{cb}", preClassB).replace("{nms}", preClassM)
                .replace("{nm}", "net.minecraft");
        return getRefClass(Class.forName(className));
    }

    /**
     * get RefClass object by real class
     *
     * @param clazz class
     * @return RefClass based on passed class
     */
    public static RefClass getRefClass(Class<?> clazz) {
        return new RefClass(clazz);
    }

    /**
     * RefClass - utility to simplify work with reflections.
     */
    public static class RefClass {

        private final Class<?> clazz;

        private RefClass(Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * get passed class
         *
         * @return class
         */
        public Class<?> getRealClass() {
            return this.clazz;
        }

        /**
         * get existing method by name and types
         *
         * @param name  name
         * @param types method parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws NoSuchMethodException if method not found
         */
        public RefMethod getMethod(String name, Object... types) throws NoSuchMethodException {
            Class<?>[] classes = new Class[types.length];
            int i = 0;
            for (Object e : types) {
                if (e instanceof Class) {
                    classes[i++] = (Class<?>) e;
                } else if (e instanceof RefClass) {
                    classes[i++] = ((RefClass) e).getRealClass();
                } else {
                    classes[i++] = e.getClass();
                }
            }
            try {
                return new RefMethod(this.clazz.getMethod(name, classes));
            } catch (NoSuchMethodException ignored) {
                return new RefMethod(this.clazz.getDeclaredMethod(name, classes));
            }
        }

        /**
         * get field by name
         *
         * @param name field name
         * @return RefField
         * @throws NoSuchFieldException if field not found
         */
        public RefField getField(String name) throws NoSuchFieldException {
            try {
                return new RefField(this.clazz.getField(name));
            } catch (NoSuchFieldException ignored) {
                return new RefField(this.clazz.getDeclaredField(name));
            }
        }

    }


    /**
     * Method wrapper
     */
    public static class RefMethod {

        private final Method method;

        private RefMethod(Method method) {
            this.method = method;
            method.setAccessible(true);
        }

        /**
         * @return passed method
         */
        public Method getRealMethod() {
            return this.method;
        }

        /**
         * apply method to object
         *
         * @param e object to which the method is applied
         * @return RefExecutor with method call(...)
         */
        public RefExecutor of(Object e) {
            return new RefExecutor(e);
        }


        public class RefExecutor {

            final Object e;

            public RefExecutor(Object e) {
                this.e = e;
            }

            /**
             * apply method for selected object
             *
             * @param params sent parameters
             * @return return value
             * @throws RuntimeException if something went wrong
             */
            public Object call(Object... params) {
                try {
                    return RefMethod.this.method.invoke(this.e, params);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }


    /**
     * Constructor wrapper
     */
    public static class RefConstructor {

        private final Constructor<?> constructor;

        private RefConstructor(Constructor<?> constructor) {
            this.constructor = constructor;
            constructor.setAccessible(true);
        }

        /**
         * create new instance with constructor
         *
         * @param params parameters for constructor
         * @return new object
         * @throws ReflectiveOperationException reflective operation exception
         * @throws IllegalArgumentException     illegal argument exception
         */
        public Object create(Object... params)
                throws ReflectiveOperationException, IllegalArgumentException {
            return this.constructor.newInstance(params);
        }

    }


    public static class RefField {

        private final Field field;

        private RefField(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        /**
         * @return passed field
         */
        public Field getRealField() {
            return this.field;
        }


        /**
         * apply fiend for object
         *
         * @param e applied object
         * @return RefExecutor with getter and setter
         */
        public RefExecutor of(Object e) {
            return new RefExecutor(e);
        }

        public class RefExecutor {

            final Object e;

            public RefExecutor(Object e) {
                this.e = e;
            }

            /**
             * set field value for applied object
             *
             * @param param value
             */
            public void set(Object param) {
                try {
                    RefField.this.field.set(this.e, param);
                } catch (Exception e) {
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
                    return RefField.this.field.get(this.e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

}
