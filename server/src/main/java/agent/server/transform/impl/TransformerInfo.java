package agent.server.transform.impl;

import java.util.*;

public class TransformerInfo {
    private final String context;
    private final Map<String, TargetClassConfig> classNameToConfig = new HashMap<>();

    public TransformerInfo(String context, List<TargetClassConfig> targetClassConfigList) {
        this.context = context;
        targetClassConfigList.forEach(targetClassConfig ->
                classNameToConfig.put(
                        targetClassConfig.targetClass.getName(),
                        targetClassConfig
                )
        );
    }

    public String getContext() {
        return context;
    }

    TargetClassConfig getTargetClassConfig(String className) {
        TargetClassConfig config = classNameToConfig.get(className);
        if (config == null)
            throw new RuntimeException("No target class config found by className: " + className);
        return config;
    }

    public List<TargetClassConfig> getTargetClassConfigList() {
        return new ArrayList<>(classNameToConfig.values());
    }

    public Set<Class<?>> getTargetClassSet() {
        Set<Class<?>> classSet = new HashSet<>();
        classNameToConfig.forEach((key, value) -> classSet.add(value.targetClass));
        return classSet;
    }
}
