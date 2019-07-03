package agent.server.transform.impl;

import agent.server.transform.config.ClassConfig;

public class TargetClassConfig {
    public final Class<?> targetClass;
    public final ClassConfig classConfig;

    public TargetClassConfig(Class<?> targetClass, ClassConfig classConfig) {
        this.targetClass = targetClass;
        this.classConfig = classConfig;
    }
}
