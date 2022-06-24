package agent.server.command.executor.script;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ExportUtils {
    private static final Predicate<Method> methodFilter = method -> {
        int modifiers = method.getModifiers();
        return !ReflectionUtils.isBridge(modifiers);
    };
    private static final Comparator<Method> methodComparator = (m1, m2) -> {
        int mod1 = m1.getModifiers();
        int mod2 = m2.getModifiers();
        if (Modifier.isStatic(mod1))
            return -1;
        else if (Modifier.isStatic(mod2))
            return 1;
        if (Modifier.isPublic(mod1))
            return -1;
        else if (Modifier.isPublic(mod2))
            return 1;
        if (Modifier.isProtected(mod1))
            return -1;
        else if (Modifier.isProtected(mod2))
            return 1;
        return m1.getName().compareTo(
                m2.getName()
        );
    };

    private static void populatePrefix(StringBuilder sb, int modifiers) {
        if (Modifier.isPublic(modifiers))
            sb.append("+");
        else if (Modifier.isPrivate(modifiers))
            sb.append("-");
        else
            sb.append(" ");

        List<String> tags = new ArrayList<>();
        if (Modifier.isStatic(modifiers))
            tags.add("static");
        if (Modifier.isAbstract(modifiers))
            tags.add("abstract");
        if (Modifier.isFinal(modifiers))
            tags.add("final");
        if (Modifier.isNative(modifiers))
            tags.add("native");
        if (Modifier.isVolatile(modifiers))
            tags.add("volatile");
        if (!tags.isEmpty())
            sb.append(
                    Utils.join(" [", "] ", ", ", tags)
            );
        else
            sb.append(" ");
    }

    private static String methodToString(Method method) {
        StringBuilder sb = new StringBuilder();
        populatePrefix(
                sb,
                method.getModifiers()
        );
        sb.append(method.getName()).append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append(
                    formatClassName(paramTypes[i])
            );
        }
        sb.append("): ").append(
                formatClassName(
                        method.getReturnType()
                )
        );
        return sb.toString();
    }

    static String formatClassName(Class<?> clazz) {
        String className = clazz.getName();
        if (className.startsWith("java.lang.")) {
            int pos = className.lastIndexOf('.');
            if (pos > -1)
                return className.substring(pos + 1);
        }
        return className;
    }

    static Collection<String> listInterfaces(Class<?> clazz) {
        List<Class<?>> clsList = new ArrayList<>();
        Set<Class<?>> rsSet = new HashSet<>();
        clsList.add(clazz);
        Class<?> cls, superCls;
        Class<?>[] intfs;
        while (!clsList.isEmpty()) {
            cls = clsList.remove(0);
            superCls = cls.getSuperclass();
            if (superCls != null)
                clsList.add(superCls);
            intfs = cls.getInterfaces();
            if (intfs != null)
                Collections.addAll(rsSet, intfs);
        }
        return rsSet.stream()
                .map(ExportUtils::formatClassName)
                .collect(Collectors.toList());
    }

    static Collection<String> listMethods(Object o, Predicate<Method> filter) {
        if (o == null)
            throw new IllegalArgumentException("Object can not be null!");
        return listMethods(
                o.getClass(),
                filter
        );
    }

    static Collection<String> listMethods(Class<?> clazz, Predicate<Method> filter) {
        return Stream.of(
                clazz.getDeclaredMethods()
        )
                .filter(
                        method -> (filter == null || filter.test(method)) &&
                                methodFilter.test(method)
                )
                .sorted(methodComparator)
                .map(ExportUtils::methodToString)
                .collect(Collectors.toList());
    }

    static Collection<String> listFields(Class<?> clazz) {
        return Stream.of(
                clazz.getDeclaredFields()
        ).map(ExportUtils::fieldToString)
                .collect(
                        Collectors.toCollection(TreeSet::new)
                );
    }

    private static String fieldToString(Field field) {
        StringBuilder sb = new StringBuilder();
        populatePrefix(
                sb,
                field.getModifiers()
        );
        sb.append(
                field.getName()
        ).append(": ").append(
                formatClassName(
                        field.getType()
                )
        );
        return sb.toString();
    }

//    private static Map<String, Object> convertToMap(Object o) {
//        if (o == null)
//            return null;
//        ReflectionUtils.get
//        o.getClass().getDe
//    }
}
