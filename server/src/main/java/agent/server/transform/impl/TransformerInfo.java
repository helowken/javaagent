package agent.server.transform.impl;

import java.util.*;

public class TransformerInfo {
    private Map<String, TargetClassConfig> namePathToConfig = new HashMap<>();

    public TransformerInfo(List<TargetClassConfig> targetClassConfigList) {
        targetClassConfigList.forEach(targetClassConfig ->
                namePathToConfig.put(getClassNamePath(targetClassConfig.targetClass), targetClassConfig)
        );
    }

    public static String getClassNamePath(Class<?> clazz) {
        final String targetClassName = clazz.getName();
        return targetClassName.replaceAll("\\.", "/");
    }

    public static String getClassName(String classNamePath) {
        return classNamePath.replaceAll("/", "\\.");
    }

    public boolean accept(ClassLoader loader, String namePath) {
        if (namePath == null)
            return false;
        TargetClassConfig config = namePathToConfig.get(namePath);
        return config != null && config.targetClass.getClassLoader().equals(loader);
    }

    public TargetClassConfig getTargetClassConfig(String namePath) {
        TargetClassConfig config = namePathToConfig.get(namePath);
        if (config == null)
            throw new RuntimeException("No target class config found by name path: " + namePath);
        return config;
    }

    public String getTargetClassName(String namePath) {
        return getTargetClassConfig(namePath).classConfig.getTargetClass();
    }

    public List<TargetClassConfig> getTargetClassConfigList() {
        return new ArrayList<>(namePathToConfig.values());
    }

    public Set<Class<?>> getTargetClassSet() {
        Set<Class<?>> classSet = new HashSet<>();
        namePathToConfig.forEach((key, value) -> classSet.add(value.targetClass));
        return classSet;
    }
}
