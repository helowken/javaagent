package agent.server.command.executor.script;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ExportUtils {
    private static final Predicate<Method> methodFilter = method -> {
        int modifiers = method.getModifiers();
        return !ReflectionUtils.isSynthetic(modifiers) &&
                !ReflectionUtils.isBridge(modifiers);
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

    private static String formatClassName(Class<?> clazz) {
        String className = clazz.getName();
        if (className.startsWith("java.lang.")) {
            int pos = className.lastIndexOf('.');
            if (pos > -1)
                return className.substring(pos + 1);
        }
        return className;
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
                .map(ExportUtils::methodToString)
                .collect(
                        Collectors.toCollection(TreeSet::new)
                );
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
}
