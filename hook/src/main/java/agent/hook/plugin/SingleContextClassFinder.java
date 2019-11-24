package agent.hook.plugin;

import agent.base.utils.SystemConfig;

class SingleContextClassFinder extends AbstractSingleContextClassFinder {
    private static final String KEY_CLASS_LOADER = "single.context.classloader";

    @Override
    protected String getClassLoaderClassName() {
        return SystemConfig.get(KEY_CLASS_LOADER);
    }
}
