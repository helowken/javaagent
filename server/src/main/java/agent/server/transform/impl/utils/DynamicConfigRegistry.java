package agent.server.transform.impl.utils;

import agent.common.utils.Registry;

import java.lang.reflect.Method;

import static agent.server.transform.config.rule.MethodRule.Position;

public class DynamicConfigRegistry {
    private static final DynamicConfigRegistry instance = new DynamicConfigRegistry();
    private final Registry<String, DynamicConfigItem> registry = new Registry<>();

    public static DynamicConfigRegistry getInstance() {
        return instance;
    }

    private DynamicConfigRegistry() {
    }

    public void reg(String key, DynamicConfigItem item) {
        registry.reg(key, item);
    }

    public DynamicConfigItem get(String key) {
        return registry.get(key);
    }

    public static class DynamicConfigItem {
        public final Position position;
        public final Method method;
        public final Object instance;

        public DynamicConfigItem(Position position, Method method, Object instance) {
            this.position = position;
            this.method = method;
            this.instance = instance;
        }
    }
}
