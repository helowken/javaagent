package agent.base.utils;

import java.net.URL;
import java.net.URLClassLoader;

public class DelegateClassLoader extends URLClassLoader {

    public DelegateClassLoader(ClassLoader parentLoader, String... libPaths) throws Exception {
        super(
                ClassLoaderUtils.findLibUrls(libPaths).toArray(new URL[0]),
                parentLoader
        );
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            Class<?> clazz = findLoadedClass(name);
            return clazz == null ? findClass(name) : clazz;
        } catch (ClassNotFoundException e) {
            return super.loadClass(name);
        }
    }
}
