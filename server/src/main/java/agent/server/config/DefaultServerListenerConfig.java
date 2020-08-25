package agent.server.config;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.ServerListener;

public class DefaultServerListenerConfig implements ServerListenerConfig {
    private final String className;

    public DefaultServerListenerConfig(String className) {
        if (Utils.isBlank(className))
            throw new IllegalArgumentException();
        this.className = className;
    }

    @Override
    public ServerListener createListener() throws Exception {
        return ReflectionUtils.newInstance(className);
    }
}
