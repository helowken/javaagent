package agent.server.transform.impl.dynamic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvalidClassNameCache {
    private static final InvalidClassNameCache instance = new InvalidClassNameCache();
    private final Map<String, Object> invalidClassNames = new ConcurrentHashMap<>();
    private final Object dummyObject = new Object();

    public static InvalidClassNameCache getInstance() {
        return instance;
    }

    public void add(String className) {
        invalidClassNames.putIfAbsent(className, dummyObject);
    }

    public boolean contains(String className) {
        return invalidClassNames.containsKey(className);
    }

    public void clear() {
        invalidClassNames.clear();
    }
}
