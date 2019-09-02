package test.utils;

import agent.hook.plugin.AbstractClassFinder;

import java.util.HashMap;
import java.util.Map;

public class TestClassFinder extends AbstractClassFinder {
    private Map<String, ClassLoader> contextToLoader = new HashMap<>();

    public void set(String context, ClassLoader loader) {
        contextToLoader.put(context, loader);
    }

    @Override
    protected void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception {
        contextToLoader.putAll(this.contextToLoader);
    }
}
