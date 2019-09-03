package test.utils;

public class TestClassLoader extends ClassLoader {

    public Class<?> defineClass(String className, byte[] bs) {
        return defineClass(className, bs, 0, bs.length);
    }
}
