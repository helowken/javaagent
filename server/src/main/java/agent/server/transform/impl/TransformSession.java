package agent.server.transform.impl;

import java.util.HashMap;
import java.util.Map;

public class TransformSession {
    private static final ThreadLocal<TransformSession> sessionLocal = new ThreadLocal<>();
    private Map<String, Map<Class<?>, byte[]>> contextToClassSet = new HashMap<>();

    public void addTransformClass(String context, Class<?> clazz, byte[] data) {
        contextToClassSet.computeIfAbsent(
                context,
                key -> new HashMap<>()
        ).put(clazz, data);
    }

    public Map<String, Map<Class<?>, byte[]>> getContextToClassSet() {
        return contextToClassSet;
    }

    public static TransformSession get() {
        TransformSession transformSession = sessionLocal.get();
        if (transformSession == null) {
            transformSession = new TransformSession();
            sessionLocal.set(transformSession);
        }
        return transformSession;
    }

    public static void clear() {
        sessionLocal.remove();
    }

}

