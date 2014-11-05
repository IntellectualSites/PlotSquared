package com.intellectualcrafters.plot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * @author DPOH-VAR
 * @version 1.0
 */
@SuppressWarnings({ "UnusedDeclaration", "rawtypes" })
public class ReflectionUtils {

    /** prefix of bukkit classes */
    private static String  preClassB = "org.bukkit.craftbukkit";
    /** prefix of minecraft classes */
    private static String  preClassM = "net.minecraft.server";
    /** boolean value, TRUE if server uses forge or MCPC+ */
    private static boolean forge     = false;

    /** check server version and class names */
    static {
        if (Bukkit.getServer() != null) {
            if (Bukkit.getVersion().contains("MCPC") || Bukkit.getVersion().contains("Forge")) {
                forge = true;
            }
            final Server server = Bukkit.getServer();
            final Class<?> bukkitServerClass = server.getClass();
            String[] pas = bukkitServerClass.getName().split("\\.");
            if (pas.length == 5) {
                final String verB = pas[3];
                preClassB += "." + verB;
            }
            try {
                final Method getHandle = bukkitServerClass.getDeclaredMethod("getHandle");
                final Object handle = getHandle.invoke(server);
                final Class handleServerClass = handle.getClass();
                pas = handleServerClass.getName().split("\\.");
                if (pas.length == 5) {
                    final String verM = pas[3];
                    preClassM += "." + verM;
                }
            }
            catch (final Exception ignored) {
            }
        }
    }

    /**
     * @return true if server has forge classes
     */
    public static boolean isForge() {
        return forge;
    }

    /**
     * Get class for name. Replace {nms} to net.minecraft.server.V*. Replace
     * {cb} to org.bukkit.craftbukkit.V*. Replace {nm} to net.minecraft
     *
     * @param classes
     *            possible class paths
     * @return RefClass object
     * @throws RuntimeException
     *             if no class found
     */
    public static RefClass getRefClass(final String... classes) {
        for (String className : classes) {
            try {
                className = className.replace("{cb}", preClassB).replace("{nms}", preClassM).replace("{nm}", "net.minecraft");
                return getRefClass(Class.forName(className));
            }
            catch (final ClassNotFoundException ignored) {
            }
        }
        throw new RuntimeException("no class found");
    }

    /**
     * get RefClass object by real class
     *
     * @param clazz
     *            class
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

        /**
         * get passed class
         *
         * @return class
         */
        public Class<?> getRealClass() {
            return this.clazz;
        }

        private RefClass(final Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * see {@link Class#isInstance(Object)}
         *
         * @param object
         *            the object to check
         * @return true if object is an instance of this class
         */
        public boolean isInstance(final Object object) {
            return this.clazz.isInstance(object);
        }

        /**
         * get existing method by name and types
         *
         * @param name
         *            name
         * @param types
         *            method parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException
         *             if method not found
         */
        public RefMethod getMethod(final String name, final Object... types) throws NoSuchMethodException {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (final Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    }
                    else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    }
                    else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefMethod(this.clazz.getMethod(name, classes));
                }
                catch (final NoSuchMethodException ignored) {
                    return new RefMethod(this.clazz.getDeclaredMethod(name, classes));
                }
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * get existing constructor by types
         *
         * @param types
         *            parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException
         *             if constructor not found
         */
        public RefConstructor getConstructor(final Object... types) {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (final Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    }
                    else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    }
                    else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefConstructor(this.clazz.getConstructor(classes));
                }
                catch (final NoSuchMethodException ignored) {
                    return new RefConstructor(this.clazz.getDeclaredConstructor(classes));
                }
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * find method by type parameters
         *
         * @param types
         *            parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException
         *             if method not found
         */
        public RefMethod findMethod(final Object... types) {
            final Class[] classes = new Class[types.length];
            int t = 0;
            for (final Object e : types) {
                if (e instanceof Class) {
                    classes[t++] = (Class) e;
                }
                else if (e instanceof RefClass) {
                    classes[t++] = ((RefClass) e).getRealClass();
                }
                else {
                    classes[t++] = e.getClass();
                }
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            findMethod: for (final Method m : methods) {
                final Class<?>[] methodTypes = m.getParameterTypes();
                if (methodTypes.length != classes.length) {
                    continue;
                }
                for (int i = 0; i < classes.length; i++) {
                    if (!classes.equals(methodTypes)) {
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
         * @param names
         *            possible names of method
         * @return RefMethod object
         * @throws RuntimeException
         *             if method not found
         */
        public RefMethod findMethodByName(final String... names) {
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
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
         * @param type
         *            type of returned value
         * @throws RuntimeException
         *             if method not found
         * @return RefMethod
         */
        public RefMethod findMethodByReturnType(final RefClass type) {
            return findMethodByReturnType(type.clazz);
        }

        /**
         * find method by return value
         *
         * @param type
         *            type of returned value
         * @return RefMethod
         * @throws RuntimeException
         *             if method not found
         */
        public RefMethod findMethodByReturnType(Class type) {
            if (type == null) {
                type = void.class;
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
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
         * @param number
         *            number of arguments
         * @return RefConstructor
         * @throws RuntimeException
         *             if constructor not found
         */
        public RefConstructor findConstructor(final int number) {
            final List<Constructor> constructors = new ArrayList<>();
            Collections.addAll(constructors, this.clazz.getConstructors());
            Collections.addAll(constructors, this.clazz.getDeclaredConstructors());
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
         * @param name
         *            field name
         * @return RefField
         * @throws RuntimeException
         *             if field not found
         */
        public RefField getField(final String name) {
            try {
                try {
                    return new RefField(this.clazz.getField(name));
                }
                catch (final NoSuchFieldException ignored) {
                    return new RefField(this.clazz.getDeclaredField(name));
                }
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * find field by type
         *
         * @param type
         *            field type
         * @return RefField
         * @throws RuntimeException
         *             if field not found
         */
        public RefField findField(final RefClass type) {
            return findField(type.clazz);
        }

        /**
         * find field by type
         *
         * @param type
         *            field type
         * @return RefField
         * @throws RuntimeException
         *             if field not found
         */
        public RefField findField(Class type) {
            if (type == null) {
                type = void.class;
            }
            final List<Field> fields = new ArrayList<>();
            Collections.addAll(fields, this.clazz.getFields());
            Collections.addAll(fields, this.clazz.getDeclaredFields());
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

        /**
         * @return passed method
         */
        public Method getRealMethod() {
            return this.method;
        }

        /**
         * @return owner class of method
         */
        public RefClass getRefClass() {
            return new RefClass(this.method.getDeclaringClass());
        }

        /**
         * @return class of method return type
         */
        public RefClass getReturnRefClass() {
            return new RefClass(this.method.getReturnType());
        }

        private RefMethod(final Method method) {
            this.method = method;
            method.setAccessible(true);
        }

        /**
         * apply method to object
         *
         * @param e
         *            object to which the method is applied
         * @return RefExecutor with method call(...)
         */
        public RefExecutor of(final Object e) {
            return new RefExecutor(e);
        }

        /**
         * call static method
         *
         * @param params
         *            sent parameters
         * @return return value
         */
        public Object call(final Object... params) {
            try {
                return this.method.invoke(null, params);
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        public class RefExecutor {
            Object e;

            public RefExecutor(final Object e) {
                this.e = e;
            }

            /**
             * apply method for selected object
             *
             * @param params
             *            sent parameters
             * @return return value
             * @throws RuntimeException
             *             if something went wrong
             */
            public Object call(final Object... params) {
                try {
                    return RefMethod.this.method.invoke(this.e, params);
                }
                catch (final Exception e) {
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

        /**
         * @return passed constructor
         */
        public Constructor getRealConstructor() {
            return this.constructor;
        }

        /**
         * @return owner class of method
         */
        public RefClass getRefClass() {
            return new RefClass(this.constructor.getDeclaringClass());
        }

        private RefConstructor(final Constructor constructor) {
            this.constructor = constructor;
            constructor.setAccessible(true);
        }

        /**
         * create new instance with constructor
         *
         * @param params
         *            parameters for constructor
         * @return new object
         * @throws RuntimeException
         *             if something went wrong
         */
        public Object create(final Object... params) {
            try {
                return this.constructor.newInstance(params);
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class RefField {
        private final Field field;

        /**
         * @return passed field
         */
        public Field getRealField() {
            return this.field;
        }

        /**
         * @return owner class of field
         */
        public RefClass getRefClass() {
            return new RefClass(this.field.getDeclaringClass());
        }

        /**
         * @return type of field
         */
        public RefClass getFieldRefClass() {
            return new RefClass(this.field.getType());
        }

        private RefField(final Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        /**
         * apply fiend for object
         *
         * @param e
         *            applied object
         * @return RefExecutor with getter and setter
         */
        public RefExecutor of(final Object e) {
            return new RefExecutor(e);
        }

        public class RefExecutor {
            Object e;

            public RefExecutor(final Object e) {
                this.e = e;
            }

            /**
             * set field value for applied object
             *
             * @param param
             *            value
             */
            public void set(final Object param) {
                try {
                    RefField.this.field.set(this.e, param);
                }
                catch (final Exception e) {
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
                }
                catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
