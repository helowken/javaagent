package agent.common.utils;

import agent.base.utils.DelegateClassLoader;
import agent.base.utils.FileUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependentClassItem {
    private static final String KEY_DEPENDENT_LIB_DIR = "dependent.lib.dir";
    private static final DependentClassItem instance = new DependentClassItem();
    private final ClassLoader parentLoader;
    private final Map<String, Class<?>> nameToClass = new ConcurrentHashMap<>();
    private ClassLoader loader;

    public static DependentClassItem getInstance() {
        return instance;
    }

    public DependentClassItem() {
        this(DependentClassItem.class.getClassLoader());
    }

    public DependentClassItem(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    private synchronized ClassLoader getLoader() throws Exception {
        if (loader == null)
            loader = new DelegateClassLoader(
                    this.parentLoader,
                    FileUtils.splitPathStringToPathArray(
                            SystemConfig.splitToSet(KEY_DEPENDENT_LIB_DIR, true),
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
