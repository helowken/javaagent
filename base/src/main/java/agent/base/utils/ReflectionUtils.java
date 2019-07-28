package agent.base.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class ReflectionUtils {
    private static final Logger logger = Logger.getLogger(ReflectionUtils.class);
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

    public static void useDeclaredFields(Object classOrClassName, AccessibleObjectConsumer<Field> consumer) throws Exception {
        Field[] fields = convert(classOrClassName).getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                exec(field, () -> {
                    consumer.consume(field);
                    return null;
                });
            }
        }
    }

    public static <T> T useField(Object classOrClassName, String fieldName, AccessibleObjectValueFunc<Field, T> func) throws Exception {
        Field field = getField(classOrClassName, fieldName);
        return exec(field, () -> func.exec(field));
    }

    private static Field getField(Object classOrClassName, String fieldName) throws Exception {
        Class<?> clazz = convert(classOrClassName);
        return findFromClassCascade(clazz,
                tmpClass -> tmpClass.getDeclaredField(fieldName)
        );
    }

    private static Class[] convertToTypes(Object... args) {
        Class<?>[] argTypes = args == null ?
                new Class<?>[0] :
                new Class<?>[args.length];
        if (argTypes.length > 0) {
            for (int i = 0; i < argTypes.length; ++i) {
                argTypes[i] = args[i].getClass();
            }
        }
        return argTypes;
    }

    public static <T> T invokeStatic(Object classOrClassName, String methodName, Object... args) throws Exception {
        return invokeStatic(classOrClassName, methodName, convertToTypes(args), args);
    }

    public static <T> T invokeStatic(Object classOrClassName, String methodName, Object[] argClassOrClassNames, Object... args) throws Exception {
        return invoke(classOrClassName, methodName, argClassOrClassNames, null, args);
    }

    public static <T> T invoke(String methodName, Object target, Object... args) throws Exception {
        return invoke(methodName, convertToTypes(args), target, args);
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

    private static <T extends AccessibleObject, V> V exec(T ao, AccessibleObjectValueSupplier<V> supplier) throws Exception {
        boolean old = ao.isAccessible();
        ao.setAccessible(true);
        try {
            return supplier.supply();
        } finally {
            ao.setAccessible(old);
        }
    }

    public static <T> T invokeMethod(Object classOrClassName, String methodName, Object[] argClassOrClassNames,
                                     AccessibleObjectValueFunc<Method, T> func) throws Exception {
        Method method = getMethod(classOrClassName, methodName, argClassOrClassNames);
        return exec(method, () -> func.exec(method));
    }

    private static Method getMethod(Object classOrClassName, String methodName, Object... argClassOrClassNames) throws Exception {
        Class<?> clazz = convert(classOrClassName);
        Class[] argTypes = convertArray(argClassOrClassNames);
        return findFromClassCascade(clazz,
                tmpClass -> tmpClass.getDeclaredMethod(methodName, argTypes)
        );
    }

    private static <T> T findFromClassCascade(Class<?> clazz, FindFunc<T> func) throws Exception {
        Class<?> tmpClass = clazz;
        Exception e = null;
        while (tmpClass != null) {
            try {
                return func.find(tmpClass);
            } catch (Exception e2) {
//                logger.debug("Error occurs on {}: {}", tmpClass, e2.getMessage());
                if (e == null)
                    e = e2;
            }
            tmpClass = tmpClass.getSuperclass();
        }
        if (e != null)
            throw e;
        throw new Exception("Unknown exception");
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
            return findClass((String) classOrClassName);
        throw new IllegalArgumentException("Argument must be a class or classname.");
    }

    public static <T> Class<T> findClass(String className) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = loader == null ?
                Class.forName(className) :
                loader.loadClass(className);
        return (Class<T>) clazz;
    }

    private static void assertTarget(Object target) {
        if (target == null)
            throw new IllegalArgumentException("Target is null!");
    }

    private interface FindFunc<V> {
        V find(Class<?> clazz) throws Exception;
    }

    private interface AccessibleObjectValueSupplier<V> {
        V supply() throws Exception;
    }

    public interface AccessibleObjectValueFunc<T extends AccessibleObject, V> {
        V exec(T ao) throws Exception;
    }

    public interface AccessibleObjectConsumer<T extends AccessibleObject> {
        void consume(T ao) throws Exception;
    }
}
