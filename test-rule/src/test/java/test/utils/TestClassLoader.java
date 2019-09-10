package test.utils;

import agent.base.utils.ReflectionUtils;

public class TestClassLoader extends ClassLoader {

    public Class<?> defineClass(String className, byte[] bs) {
        return defineClass(className, bs, 0, bs.length);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);
        if (clazz != null)
            return clazz;
        return super.loadClass(name, resolve);
    }
}
