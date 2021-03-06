package agent.base.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class InvokeDescriptorUtils {
    public static final String JAVA_LANG_PACKAGE = "java.lang.";
    public static final int JAVA_LANG_PACKAGE_LENGTH = JAVA_LANG_PACKAGE.length();
    private static final Map<Class<?>, String> classToTypeDesc = new HashMap<>();
    private static final Map<String, String> descToPrimitiveName = new HashMap<>();

    static {
        classToTypeDesc.put(boolean.class, "Z");
        classToTypeDesc.put(byte.class, "B");
        classToTypeDesc.put(char.class, "C");
        classToTypeDesc.put(short.class, "S");
        classToTypeDesc.put(int.class, "I");
        classToTypeDesc.put(long.class, "J");
        classToTypeDesc.put(float.class, "F");
        classToTypeDesc.put(double.class, "D");
        classToTypeDesc.put(void.class, "V");

        classToTypeDesc.forEach(
                (clazz, desc) -> descToPrimitiveName.put(
                        desc, clazz.getName()
                )
        );
    }

    public static String getLongName(Method method) {
        return method.getDeclaringClass().getName() + "." + getFullDescriptor(method);
    }

    public static String getLongName(Constructor constructor) {
        return constructor.getDeclaringClass().getName() + "." + getFullDescriptor(constructor);
    }

    public static String getFullDescriptor(Method method) {
        return method.getName() + getDescriptor(method);
    }

    public static String getFullDescriptor(Constructor constructor) {
        return ReflectionUtils.CONSTRUCTOR_NAME + getDescriptor(constructor);
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
                    .map(InvokeDescriptorUtils::getTypeDescriptor)
                    .forEach(sb::append);
        sb.append(")").append(
                getTypeDescriptor(returnType)
        );
        return sb.toString();
    }

    private static String getTypeDescriptor(Class<?> clazz) {
        if (clazz == null)
            return "";
        return Optional.ofNullable(
                classToTypeDesc.get(clazz)
        ).orElseGet(
                () -> clazz.isArray() ?
                        "[" + getTypeDescriptor(clazz.getComponentType()) :
                        "L" + getFullQualifiedClassName(clazz) + ";"
        );
    }

    private static String getFullQualifiedClassName(Class<?> clazz) {
        return clazz.getName().replaceAll("\\.", "/");
    }

    public static String descToText(String desc) {
        return descToText(desc, new TextConfig());
    }

    public static String descToText(String desc, TextConfig config) {
        int pos = desc.indexOf("(");
        int pos2 = desc.indexOf(")");
        if (pos == -1 || pos2 == -1)
            throw new IllegalArgumentException("Invalid method desc: " + desc);
        String name = "";
        if (pos > 0)
            name = desc.substring(0, pos);
        String returnType = desc.substring(pos2 + 1);
        String returnTypeText = null;

        boolean withReturnType = config.withReturnType;
        if (ReflectionUtils.CONSTRUCTOR_NAME.equals(name))
            withReturnType = false;

        StringBuilder sb = new StringBuilder();
        if (withReturnType) {
            returnTypeText = typeDescToText(returnType, config);
            if (!config.returnTypeAtTheEnd)
                sb.append(returnTypeText).append(" ");
        }

        sb.append(name);
        sb.append("(");
        String paramTypesDesc = desc.substring(pos + 1, pos2);
        String text;
        int nextIdx;
        for (int i = 0, len = paramTypesDesc.length(); i < len; ) {
            String typeDesc = String.valueOf(
                    paramTypesDesc.charAt(i)
            );
            if (i > 0)
                sb.append(", ");
            if (descToPrimitiveName.containsKey(typeDesc)) {
                text = typeDescToText(typeDesc, config);
                nextIdx = i + 1;
            } else {
                if (typeDesc.equals("[")) {
                    int k = i + 1;
                    while (paramTypesDesc.charAt(k) == '[') {
                        ++k;
                    }
                    if (paramTypesDesc.charAt(k) == 'L')
                        nextIdx = findEndPosOfClass(paramTypesDesc, k + 1) + 1;
                    else
                        nextIdx = k + 1;
                } else
                    nextIdx = findEndPosOfClass(paramTypesDesc, i + 1) + 1;
                text = typeDescToText(
                        paramTypesDesc.substring(i, nextIdx),
                        config
                );
            }
            i = nextIdx;
            sb.append(text);
        }
        sb.append(")");
        if (withReturnType && config.returnTypeAtTheEnd) {
            sb.append(":").append(returnTypeText);
        }
        return sb.toString();
    }

    private static int findEndPosOfClass(String desc, int startPos) {
        int end = desc.indexOf(";", startPos);
        if (end == -1)
            throw new IllegalArgumentException("Invalid desc: " + desc);
        return end;
    }

    private static String typeDescToText(String typeDesc, TextConfig config) {
        StringBuilder sb = new StringBuilder();
        int pos = typeDesc.lastIndexOf("[");
        String realTypeDesc = typeDesc;
        if (pos > -1) {
            realTypeDesc = typeDesc.substring(pos + 1);
        }
        String primitiveName = descToPrimitiveName.get(realTypeDesc);
        if (primitiveName != null)
            sb.append(primitiveName);
        else {
            if (!realTypeDesc.startsWith("L") || !realTypeDesc.endsWith(";"))
                throw new IllegalArgumentException("Invalid desc: " + typeDesc);
            String className = realTypeDesc.substring(1, realTypeDesc.length() - 1).replaceAll("/", ".");
            if (!config.withPkg)
                className = getSimpleName(className);
            else if (config.shortForPkgLang)
                className = shortForPkgLang(className);
            sb.append(className);
        }
        for (int i = 0, len = pos + 1; i < len; ++i) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public static String shortForPkgLang(String className) {
        int pos = className.lastIndexOf('.');
        return pos > -1 &&
                pos == JAVA_LANG_PACKAGE_LENGTH - 1 &&
                className.startsWith(JAVA_LANG_PACKAGE) ?
                className.substring(JAVA_LANG_PACKAGE_LENGTH) :
                className;
    }

    public static String getSimpleName(String className) {
        int lastPos = className.lastIndexOf(".");
        return lastPos > -1 ?
                className.substring(lastPos + 1) :
                className;
    }

    public static class TextConfig {
        public boolean shortForPkgLang = true;
        public boolean withPkg = true;
        public boolean withReturnType = true;
        public boolean returnTypeAtTheEnd = true;
    }
}
