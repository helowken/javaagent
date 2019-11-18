package test.server.utils;

import java.net.URL;
import java.net.URLClassLoader;

public class TestClassLoader extends URLClassLoader {
    public TestClassLoader() {
        this(new URL[0]);
    }

    public TestClassLoader(URL[] urls) {
        super(urls);
    }

    public Class<?> loadClass(String className, byte[] data) {
        return defineClass(className, data, 0, data.length);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            return findClass(className);
        } catch (ClassNotFoundException e) {
        }
        return super.loadClass(className);
    }
}
