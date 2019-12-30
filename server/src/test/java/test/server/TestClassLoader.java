package test.server;

public class TestClassLoader extends ClassLoader {
    public Class<?> loadClass(String className, byte[] data) {
        return defineClass(className, data, 0, data.length);
    }

}
