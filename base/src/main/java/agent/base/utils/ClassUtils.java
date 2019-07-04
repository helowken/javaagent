package agent.base.utils;

import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class ClassUtils {
    private static final String[] javaPackages = {"java.", "javax.", "sun."};

    public static boolean isJavaNativePackage(String namePath) {
        for (String javaPackage : javaPackages) {
            if (namePath.startsWith(javaPackage))
                return true;
        }
        return false;
    }

    public static <T, V> V invokeStatic(String className, String methodName, Class<?>[] argTypes, Object[] args) throws Exception {
        return invokeStatic(Thread.currentThread().getContextClassLoader(), className, methodName, argTypes, args);
    }

    public static <T, V> V invokeStatic(ClassLoader loader, String className, String methodName, Class<?>[] argTypes, Object[] args) throws Exception {
        Class<T> clazz = (Class<T>) loader.loadClass(className);
        return invokeStatic(clazz, methodName, argTypes, args);
    }

    public static <T, V> V invokeStatic(Class<T> clazz, String methodName, Class<?>[] argTypes, Object[] args) throws Exception {
        return invoke(clazz, methodName, argTypes, null, args);
    }

    public static <T, V> V invoke(String className, String methodName, Class<?>[] argTypes, T instance, Object[] args) throws Exception {
        return invoke(Thread.currentThread().getContextClassLoader(), className, methodName, argTypes, instance, args);
    }

    public static <T, V> V invoke(ClassLoader loader, String className, String methodName, Class<?>[] argTypes, T instance, Object[] args) throws Exception {
        Class<T> clazz = (Class<T>) loader.loadClass(className);
        return invoke(clazz, methodName, argTypes, instance, args);
    }

    public static <T, V> V invoke(Class<T> clazz, String methodName, Class<?>[] argTypes, T instance, Object[] args) throws Exception {
        if (argTypes == null)
            argTypes = new Class<?>[0];
        if (args == null)
            args = new Object[0];
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            method = clazz.getMethod(methodName, argTypes);
        }
        return (V) method.invoke(instance, args);
    }
}
