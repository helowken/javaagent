package agent.hook.plugin;

import agent.jvmti.JvmtiUtils;

import java.util.List;

public abstract class AbstractSingleContextClassFinder extends AbstractClassFinder {
    private LoaderItem loaderItem;

    protected abstract String getClassLoaderClassName();

    @Override
    LoaderItem findLoaderItemByContext(String contextPath) {
        init(() -> {
            String loaderClassName = getClassLoaderClassName();
            List<ClassLoader> loaderList = JvmtiUtils.getInstance()
                    .findObjectsByClassName(loaderClassName, Integer.MAX_VALUE);
            if (loaderList.isEmpty())
                throw new RuntimeException("No ClassLoader found by: " + loaderClassName);
            int size = loaderList.size();
            if (size > 1)
                throw new RuntimeException("Found ClassLoader number is " + size + ", it is more than 1.");
            loaderItem = new LoaderItem(
                    loaderList.get(0)
            );
        });
        return loaderItem;
    }
}
