package agent.base.utils;

public class DelegateClassLoader extends ClassLoader {
    private final ClassLoader childLoader;
    private final ClassLoader parentLoader;

    public DelegateClassLoader(ClassLoader parentLoader, String... libPaths) throws Exception {
        this.parentLoader = parentLoader;
        this.childLoader = ClassLoaderUtils.newURLClassLoader(null, libPaths);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return childLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (parentLoader != null)
                return parentLoader.loadClass(name);
            throw e;
        }
    }
}
