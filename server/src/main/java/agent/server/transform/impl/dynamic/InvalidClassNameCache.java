package agent.server.transform.impl.dynamic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InvalidClassNameCache {
    private static final InvalidClassNameCache instance = new InvalidClassNameCache();
    private final Map<String, Map<String, Object>> contextToInvalidClassNames = new ConcurrentHashMap<>();
    private final Object dummyObject = new Object();

    public static InvalidClassNameCache getInstance() {
        return instance;
    }

    public void add(String context, String className) {
        contextToInvalidClassNames.computeIfAbsent(
                context,
                key -> new ConcurrentHashMap<>()
        ).putIfAbsent(className, dummyObject);
    }

    public boolean contains(String context, String className) {
        return Optional.ofNullable(
                contextToInvalidClassNames.get(context)
        ).map(
                classNameMap -> classNameMap.containsKey(className)
        ).orElse(false);
    }

    public void clear() {
        contextToInvalidClassNames.clear();
    }

    public Collection<String> getInvalidClassNames(String context) {
        return new LinkedList<>(
                contextToInvalidClassNames.getOrDefault(
                        context,
                        Collections.emptyMap()
                ).keySet()
        );
    }
}
