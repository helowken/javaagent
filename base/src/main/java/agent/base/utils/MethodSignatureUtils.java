package agent.base.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MethodSignatureUtils {
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
        return method.getDeclaringClass().getName() + "." + getFullSignature(method);
    }

    public static String getFullSignature(Method method) {
        return method.getName() + getSignature(method);
    }

    public static String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes != null)
            Stream.of(paramTypes)
                    .map(MethodSignatureUtils::getTypeSignature)
                    .forEach(sb::append);
        sb.append(")").append(
                getTypeSignature(
                        method.getReturnType()
                )
        );
        return sb.toString();
    }

    private static String getTypeSignature(Class<?> clazz) {
        return Optional.ofNullable(
                classToTypeSignature.get(clazz)
        ).orElseGet(
                () -> clazz.isArray() ?
                        "[" + getTypeSignature(clazz.getComponentType()) :
                        "L" + getFullQualifiedClassName(clazz) + ";"
        );
    }

    private static String getFullQualifiedClassName(Class<?> clazz) {
        return clazz.getName().replaceAll("\\.", "/");
    }

}
