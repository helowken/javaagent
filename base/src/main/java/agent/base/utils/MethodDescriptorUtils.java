package agent.base.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MethodDescriptorUtils {
    private static final Map<Class<?>, String> classToTypeSignature = new HashMap<>();

    static {
        classToTypeSignature.put(boolean.class, "Z");
        classToTypeSignature.put(byte.class, "B");
        classToTypeSignature.put(char.class, "C");
        classToTypeSignature.put(short.class, "S");
        classToTypeSignature.put(int.class, "I");
        classToTypeSignature.put(long.class, "J");
        classToTypeSignature.put(float.class, "F");
        classToTypeSignature.put(double.class, "D");
        classToTypeSignature.put(void.class, "V");
    }

    public static String getLongName(Method method) {
        return method.getDeclaringClass().getName() + "." + getFullDescriptor(method);
    }

    public static String getFullDescriptor(Method method) {
        return method.getName() + getDescriptor(method);
    }

    public static String getDescriptor(Method method) {
        return getDescriptor(
                method.getParameterTypes(),
                method.getReturnType()
        );
    }

    public static String getDescriptor(Constructor constructor) {
        return getDescriptor(
                constructor.getParameterTypes(),
                void.class
        );
    }

    private static String getDescriptor(Class<?>[] paramTypes, Class<?> returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (paramTypes != null)
            Stream.of(paramTypes)
                    .map(MethodDescriptorUtils::getTypeDescriptor)
                    .forEach(sb::append);
        sb.append(")").append(
                getTypeDescriptor(returnType)
        );
        return sb.toString();
    }

    private static String getTypeDescriptor(Class<?> clazz) {
        return Optional.ofNullable(
                classToTypeSignature.get(clazz)
        ).orElseGet(
                () -> clazz.isArray() ?
                        "[" + getTypeDescriptor(clazz.getComponentType()) :
                        "L" + getFullQualifiedClassName(clazz) + ";"
        );
    }

    private static String getFullQualifiedClassName(Class<?> clazz) {
        return clazz.getName().replaceAll("\\.", "/");
    }

}
