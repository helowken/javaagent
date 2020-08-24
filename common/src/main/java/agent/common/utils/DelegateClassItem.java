package agent.common.utils;

import agent.base.utils.DelegateClassLoader;
import agent.base.utils.FileUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelegateClassItem {
    private static final String KEY_DELEGATE_LIB_DIR = "delegate.lib.dir";
    private static final DelegateClassItem instance = new DelegateClassItem();
    private final ClassLoader parentLoader;
    private final Map<String, Class<?>> nameToClass = new ConcurrentHashMap<>();
    private ClassLoader loader;

    public static DelegateClassItem getInstance() {
        return instance;
    }

    public DelegateClassItem() {
        this(DelegateClassItem.class.getClassLoader());
    }

    public DelegateClassItem(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    private synchronized ClassLoader getLoader() throws Exception {
        if (loader == null)
            loader = new DelegateClassLoader(
                    this.parentLoader,
                    FileUtils.splitPathStringToPathArray(
                            SystemConfig.splitToSet(KEY_DELEGATE_LIB_DIR),
                            SystemConfig.getBaseDir()
                    )
            );
        return loader;
    }

    public Class<?> getDelegateClass(String className) {
        return nameToClass.computeIfAbsent(
                className,
                key -> Utils.wrapToRtError(
                        () -> getLoader().loadClass(className)
                )
        );
    }

    public void mock(String className, Class<?> clazz) {
        nameToClass.put(className, clazz);
    }
}
