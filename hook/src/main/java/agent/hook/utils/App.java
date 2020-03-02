package agent.hook.utils;

import agent.base.plugin.PluginFactory;
import agent.hook.plugin.ClassFinder;

public class App {
    public static volatile Object instance;

    public static ClassFinder getClassFinder() {
        return PluginFactory.getInstance().find(ClassFinder.class,
                AppTypePluginFilter.getInstance()
        );
    }

    public static ClassLoader getLoader(String context) {
        return getClassFinder().findClassLoader(context);
    }
}
