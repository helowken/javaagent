package agent.server.listener;

import agent.base.plugin.PluginFactory;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.hook.plugin.AppHook;
import agent.hook.utils.AppTypePluginFilter;
import agent.hook.utils.AttachType;
import agent.hook.utils.HookConstants;
import agent.server.ServerListener;

public class HookAppListener implements ServerListener {

    @Override
    public void onStartup(Object[] args) {
        Utils.wrapToRtError(
                () -> PluginFactory.getInstance()
                        .find(
                                AppHook.class,
                                AppTypePluginFilter.getInstance()
                        )
                        .hook(
                                AttachType.valueOf(
                                        SystemConfig.get(HookConstants.KEY_ATTACH_TYPE)
                                )
                        )
        );
    }

    @Override
    public void onShutdown() {

    }
}
