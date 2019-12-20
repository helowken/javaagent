package test.transformer;

import agent.base.utils.Utils;
import agent.hook.plugin.ClassFinder;

public class TestClassFinder implements ClassFinder {
    @Override
    public ClassLoader findClassLoader(String contextPath) {
        return getClass().getClassLoader();
    }

    @Override
    public void setParentClassLoader(String contextPath, ClassLoader parentLoader) {
    }

    @Override
    public Class<?> findClass(String contextPath, String className) {
        return Utils.wrapToRtError(
                () -> findClassLoader(contextPath).loadClass(className)
        );
    }
}
