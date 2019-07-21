package agent.base.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class ReflectionUtils {
    private static final String[] javaPackages = {"java.", "javax.", "sun."};

    public static boolean isJavaNativePackage(String namePath) {
        for (String javaPackage : javaPackages) {
            if (namePath.startsWith(javaPackage))
                return true;
        }
        return false;
    }

    public static <T> T newInstance(Object classOrClassName) throws Exception {
        return newInstance(classOrClassName, new Class[0]);
    }

    public static <T> T newInstance(Object classOrClassName, Object[] argClassOrClassNames, Object... args) throws Exception {
        Class[] argTypes = convertArray(argClassOrClassNames);
        Constructor<T> constructor = (Constructor<T>) convert(classOrClassName).getDeclaredConstructor(argTypes);
        return exec(constructor, () -> constructor.newInstance(args));
    }

    public static void setStaticFieldValue(Object classOrClassName, String fieldName, Object value) throws Exception {
        setFieldValue(classOrClassName, fieldName, null, value);
    }

    public static void setFieldValue(String fieldName, Object target, Object value) throws Exception {
        assertTarget(target);
        setFieldValue(target.getClass(), fieldName, target, value);
    }

    public static void setFieldValue(Object classOrClassName, String fieldName, Object target, Object value) throws Exception {
        Field field = getField(classOrClassName, fieldName);
        exec(field, () -> {
            field.set(target, value);
            return null;
        });
    }

    public static <T> T getStaticFieldValue(Object classOrClassName, String fieldName) throws Exception {
        return getFieldValue(classOrClassName, fieldName, null);
    }

    public static <T> T getFieldValue(String fieldName, Object target) throws Exception {
        assertTarget(target);
        return getFieldValue(target.getClass(), fieldName, target);
    }

    public static <T> T getFieldValue(Object classOrClassName, String fieldName, Object target) throws Exception {
        Field field = getField(classOrClassName, fieldName);
        return exec(field, () -> (T) field.get(target));
    }

    private static Field getField(Object classOrClassName, String fieldName) throws Exception {
        return convert(classOrClassName).getDeclaredField(fieldName);
    }

    public static <T> T invokeStatic(Object classOrClassName, String methodName) throws Exception {
        return invokeStatic(classOrClassName, methodName, new Class[0]);
    }

    public static <T> T invokeStatic(Object classOrClassName, String methodName, Object[] argClassOrClassNames, Object... args) throws Exception {
        return invoke(classOrClassName, methodName, argClassOrClassNames, null, args);
    }

    public static <T> T invoke(String methodName, Object target) throws Exception {
        return invoke(methodName, new Class[0], target);
    }

    public static <T> T invoke(String methodName, Object[] argClassOrClassNames, Object target, Object... args) throws Exception {
        assertTarget(target);
        return invoke(target.getClass(), methodName, argClassOrClassNames, target, args);
    }

    public static <T> T invoke(Object classOrClassName, String methodName, Object[] argClassOrClassNames, Object target, Object... args) throws Exception {
        Method method = getMethod(classOrClassName, methodName, argClassOrClassNames);
        return exec(method, () -> (T) method.invoke(target, args));
    }

    private static <T extends AccessibleObject, V> V exec(T ao, AccessibleObjectFunc<V> func) throws Exception {
        boolean old = ao.isAccessible();
        ao.setAccessible(true);
        try {
            return func.supply();
        } finally {
            ao.setAccessible(old);
        }
    }

    public static <T> T invokeMethod(Object classOrClassName, String methodName, Object[] argClassOrClassNames,
                                     AccessibleObjectConsumer<Method, T> consumer) throws Exception {
        Method method = getMethod(classOrClassName, methodName, argClassOrClassNames);
        return exec(method, () -> consumer.supply(method));
    }

    private static Method getMethod(Object classOrClassName, String methodName, Object... argClassOrClassNames) throws Exception {
        Class<?> clazz = convert(classOrClassName);
        Class[] argTypes = convertArray(argClassOrClassNames);
        try {
            return clazz.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getMethod(methodName, argTypes);
            } catch (NoSuchMethodException e2) {
                Class<?> superClass = clazz.getSuperclass();
                while (superClass != null) {
                    try {
                        return superClass.getDeclaredMethod(methodName, argTypes);
                    } catch (NoSuchMethodException e3) {
                    }
                    superClass = superClass.getSuperclass();
                }
                throw e2;
            }
        }
    }

    private static Class[] convertArray(Object... classOrClassNames) throws Exception {
        if (classOrClassNames == null)
            return new Class[0];
        Class[] classes = new Class[classOrClassNames.length];
        for (int i = 0; i < classOrClassNames.length; ++i) {
            classes[i] = convert(classOrClassNames[i]);
        }
        return classes;
    }

    private static Class<?> convert(Object classOrClassName) throws Exception {
        if (classOrClassName instanceof Class)
            return (Class<?>) classOrClassName;
        else if (classOrClassName instanceof String)
            return Class.forName((String) classOrClassName);
        throw new IllegalArgumentException("Argument must be a class or classname.");
    }

    private static void assertTarget(Object target) {
        if (target == null)
            throw new IllegalArgumentException("Target is null!");
    }

    private interface AccessibleObjectFunc<V> {
        V supply() throws Exception;
    }

    public interface AccessibleObjectConsumer<T extends AccessibleObject, V> {
        V supply(T ao) throws Exception;
    }
}
