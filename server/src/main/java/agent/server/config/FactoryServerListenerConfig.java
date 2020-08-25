package agent.server.config;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.ServerListener;

public class FactoryServerListenerConfig implements ServerListenerConfig {
    private final String factoryClass;
    private final String factoryMethod;

    public FactoryServerListenerConfig(String factoryClass, String factoryMethod) {
        if (Utils.isBlank(factoryClass) || Utils.isBlank(factoryMethod))
            throw new IllegalArgumentException();
        this.factoryClass = factoryClass;
        this.factoryMethod = factoryMethod;
    }

    @Override
    public ServerListener createListener() throws Exception {
        return ReflectionUtils.invokeStatic(
                factoryClass,
                factoryMethod,
                new Class[0]
        );
    }
}
