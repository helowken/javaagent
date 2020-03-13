package test.server;

public class TestClassLoader extends ClassLoader {

    public TestClassLoader() {
        super();
    }

    public TestClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(String className, byte[] data) {
        Class<?> clazz = this.findLoadedClass(className);
        return clazz != null ?
                clazz :
                defineClass(className, data, 0, data.length);
    }

}
