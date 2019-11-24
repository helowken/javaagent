package agent.hook.plugin;

import agent.hook.utils.App;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMultiContextClassFinder extends AbstractClassFinder {
    private final Map<String, LoaderItem> contextPathToClassLoader = new HashMap<>();

    protected abstract void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception;

    @Override
    LoaderItem findLoaderItemByContext(String contextPath) {
        init(() -> {
            if (App.instance != null) {
                Map<String, ClassLoader> tmp = new HashMap<>();
                doInit(App.instance, tmp);
                tmp.forEach(
                        (context, classLoader) -> contextPathToClassLoader.put(
                                context,
                                new LoaderItem(classLoader)
                        )
                );
            } else
                throw new RuntimeException("No app instance found.");
        });
        return contextPathToClassLoader.get(contextPath);
    }

}
