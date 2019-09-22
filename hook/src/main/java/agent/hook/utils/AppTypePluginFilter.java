package agent.hook.utils;

import agent.base.plugin.InfoPluginFilter;
import agent.base.utils.SystemConfig;

public class AppTypePluginFilter extends InfoPluginFilter {
    private static final AppTypePluginFilter instance = new AppTypePluginFilter();

    public static AppTypePluginFilter getInstance() {
        return instance;
    }

    private AppTypePluginFilter() {
        super(
                HookConstants.KEY_APP_TYPE,
                SystemConfig.get(HookConstants.KEY_APP_TYPE)
        );
    }
}
