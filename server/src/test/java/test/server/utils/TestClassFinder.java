package test.server.utils;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.plugin.AbstractClassFinder;

import java.util.Map;

public class TestClassFinder extends AbstractClassFinder {
    private Map<String, LoaderItem> contextToLoader;

    public TestClassFinder() {
        Utils.wrapToRtError(
                () -> contextToLoader = ReflectionUtils.getFieldValue("contextPathToClassLoader", this)
        );
    }

    public void set(String context, ClassLoader loader) {
        contextToLoader.put(context, new LoaderItem(loader));
    }

    public void setContextLoader(String context) {
        set(context, Thread.currentThread().getContextClassLoader());
    }

    public void reset() {
        contextToLoader.clear();
    }

    @Override
    protected void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception {
    }
}
