package com.intellectualcrafters.plot.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public static Method findMethod(Class<?> clazz, boolean isStatic, Class<?> returnType, Class... types) {
        loop:
        for (Method method : clazz.getMethods()) {
            Class<?> result = method.getReturnType();
            Class<?>[] param = method.getParameterTypes();
            int paramCount = param.length;
            boolean stc = Modifier.isStatic(method.getModifiers());
            if (stc == isStatic && result == returnType && types.length == paramCount) {
                for (int i = 0; i < types.length; i++) {
                    if (types[i] != param[i]) {
                        continue loop;
                    }
                }
                method.setAccessible(true);
                return method;
            }
        }
        throw new RuntimeException("no such method");
    }

    public static Field findField(Class<?> clazz, Class<?> fieldClass) {
        for (Field field : clazz.getFields()) {
            if (fieldClass == field.getType() || fieldClass.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static <T> List<T> getStaticFields(Class clazz) {
        ArrayList<T> list = new ArrayList<T>();
        try {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                Object value = field.get(null);
                try {
                    list.add((T) value);
                } catch (ClassCastException ignored) {}
            }
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Class<?> getNmsClass(String name) {
        String className = preClassM + '.' + name;
        return getClass(className);
    }

    public static Class<?> getCbClass(String name) {
        String className = preClassB + '.' + name;
        return getClass(className);
    }

    public static Class<?> getUtilClass(String name) {
        try {
            return Class.forName(name); //Try before 1.8 first
        } catch (ClassNotFoundException ignored) {}
        try {
            return Class.forName("net.minecraft.util." + name); //Not 1.8
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static Object getHandle(Object wrapper) {
        Method getHandle = makeMethod(wrapper.getClass(), "getHandle");
        return callMethod(getHandle, wrapper);
    }

    //Utils
    public static Method makeMethod(Class<?> clazz, String methodName, Class<?>... paramaters) {
        try {
            return clazz.getDeclaredMethod(methodName, paramaters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(Method method, Object instance, Object... paramaters) {
        if (method == null) {
            throw new RuntimeException("No such method");
        }
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> makeConstructor(Class<?> clazz, Class<?>... paramaterTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(paramaterTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    public static <T> T callConstructor(Constructor<T> constructor, Object... paramaters) {
        if (constructor == null) {
            throw new RuntimeException("No such constructor");
        }
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field makeField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object instance) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        if (field == null) {
            throw new RuntimeException("No such field");
        }
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
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
     *
     * @return RefClass object
     *
     * @throws ClassNotFoundException if no class found
     */
    public static RefClass getRefClass(String className) throws ClassNotFoundException {
        className = className.replace("{cb}", preClassB).replace("{nms}", preClassM).replace("{nm}", "net.minecraft");
        return getRefClass(Class.forName(className));
    }

    /**
     * get RefClass object by real class
     *
     * @param clazz class
     *
     * @return RefClass based on passed class
     */
    public static RefClass getRefClass(Class clazz) {
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
         * see {@link Class#isInstance(Object)}
         *
         * @param object the object to check
         *
         * @return true if object is an instance of this class
         */
        public boolean isInstance(Object object) {
            return this.clazz.isInstance(object);
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
        public RefMethod getMethod(String name, Object... types) throws NoSuchMethodException {
            Class[] classes = new Class[types.length];
            int i = 0;
            for (Object e : types) {
                if (e instanceof Class) {
                    classes[i++] = (Class) e;
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
         * get existing constructor by types
         *
         * @param types parameters. can be Class or RefClass
         *
         * @return RefMethod object
         *
         * @throws RuntimeException if constructor not found
         */
        public RefConstructor getConstructor(Object... types) throws NoSuchMethodException {
            Class[] classes = new Class[types.length];
            int i = 0;
            for (Object e : types) {
                if (e instanceof Class) {
                    classes[i++] = (Class) e;
                } else if (e instanceof RefClass) {
                    classes[i++] = ((RefClass) e).getRealClass();
                } else {
                    classes[i++] = e.getClass();
                }
            }
            try {
                return new RefConstructor(this.clazz.getConstructor(classes));
            } catch (NoSuchMethodException ignored) {
                return new RefConstructor(this.clazz.getDeclaredConstructor(classes));
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
        public RefMethod findMethod(Object... types) {
            Class[] classes = new Class[types.length];
            int t = 0;
            for (Object e : types) {
                if (e instanceof Class) {
                    classes[t++] = (Class) e;
                } else if (e instanceof RefClass) {
                    classes[t++] = ((RefClass) e).getRealClass();
                } else {
                    classes[t++] = e.getClass();
                }
            }
            List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            findMethod:
            for (Method m : methods) {
                Class<?>[] methodTypes = m.getParameterTypes();
                if (methodTypes.length != classes.length) {
                    continue;
                }
                for (Class aClass : classes) {
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
        public RefMethod findMethodByName(String... names) {
            List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            for (Method m : methods) {
                for (String name : names) {
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
        public RefMethod findMethodByReturnType(RefClass type) {
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
            List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            for (Method m : methods) {
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
        public RefConstructor findConstructor(int number) {
            List<Constructor> constructors = new ArrayList<>();
            Collections.addAll(constructors, this.clazz.getConstructors());
            Collections.addAll(constructors, this.clazz.getDeclaredConstructors());
            for (Constructor m : constructors) {
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
        public RefField getField(String name) throws NoSuchFieldException {
            try {
                return new RefField(this.clazz.getField(name));
            } catch (NoSuchFieldException ignored) {
                return new RefField(this.clazz.getDeclaredField(name));
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
        public RefField findField(RefClass type) {
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
            List<Field> fields = new ArrayList<>();
            Collections.addAll(fields, this.clazz.getFields());
            Collections.addAll(fields, this.clazz.getDeclaredFields());
            for (Field f : fields) {
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

        /**
         * apply method to object
         *
         * @param e object to which the method is applied
         *
         * @return RefExecutor with method call(...)
         */
        public RefExecutor of(Object e) {
            return new RefExecutor(e);
        }

        /**
         * call static method
         *
         * @param params sent parameters
         *
         * @return return value
         */
        public Object call(Object... params) {
            try {
                return this.method.invoke(null, params);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
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
             *
             * @return return value
             *
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

        private final Constructor constructor;

        private RefConstructor(Constructor constructor) {
            this.constructor = constructor;
            constructor.setAccessible(true);
        }

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

        /**
         * create new instance with constructor
         *
         * @param params parameters for constructor
         *
         * @return new object
         *
         * @throws ReflectiveOperationException
         * @throws IllegalArgumentException
         */
        public Object create(Object... params) throws ReflectiveOperationException, IllegalArgumentException {
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

        /**
         * apply fiend for object
         *
         * @param e applied object
         *
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
