package test;

import org.junit.Test;
import sun.misc.Launcher;
import sun.net.www.ParseUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoaderTest {

    @Test
    public void test() throws Exception {
//        System.out.println(System.getProperty("java.ext.dirs"));
//        if (true) {
//            return;
//        }
//        System.out.println("=====================");
//        System.out.println(System.getProperty("sun.boot.class.path"));

        Class[] declaredClasses = Launcher.class.getDeclaredClasses();
        Class<?> appClassLoaderClass = null;
        if (declaredClasses != null) {
            for (Class<?> declaredClass : declaredClasses) {
                if (declaredClass.getName().endsWith("AppClassLoader")) {
                    appClassLoaderClass = declaredClass;
                    break;
                }
            }
        }
        if (appClassLoaderClass != null) {
            Method method = appClassLoaderClass.getDeclaredMethod("getAppClassLoader", ClassLoader.class);
            method.setAccessible(true);
            ClassLoader appClassLoader = (ClassLoader) method.invoke(null, new Object[]{null});
            Class<?> stringClass = appClassLoader.loadClass("com.sun.nio.zipfs.ZipPath");
            System.out.println(stringClass == String.class);
        }
    }

    @Test
    public void testJarLoaderForRemote() throws Exception {
        URL[] urls = new URL[]{
                new URL("http://localhost:8080/test/repository/lib/entity.jar")
        };
        ClassLoader classLoader = new URLClassLoader(urls, null);
        Class<?> aClass = classLoader.loadClass("test.entity.A");
        System.out.println(aClass.getName());
    }

    @Test
    public void testJarLoaderForLocal() throws Exception {
        URL[] urls = new URL[]{
                ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/entity.jar"))
        };
        ClassLoader classLoader = new URLClassLoader(urls, null);
        Class<?> aClass = classLoader.loadClass("test.entity.A");
        System.out.println(aClass.getName());
    }

    @Test
    public void testLoader() throws Exception {
        URL[] urls = new URL[]{
                new URL("http://localhost:8080/test/repository/classes/")
        };
        ClassLoader classLoader = new URLClassLoader(urls, null);
        Class<?> aClass = classLoader.loadClass("test.entity.A");
        System.out.println(aClass.getName());
    }

    @Test
    public void testFileLoader() throws Exception {
        URL[] urls = new URL[]{
                ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/classes"))
        };
        ClassLoader classLoader = new URLClassLoader(urls, null);
        Class<?> aClass = classLoader.loadClass("test.entity.A");
        System.out.println(aClass.getName());
    }

}
