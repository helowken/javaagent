package test;

import agent.base.utils.ReflectionUtils;
import org.junit.Test;
import sun.misc.Launcher;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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
    public void testFileLoaderGetResource() throws Exception {
        URLClassPath ucp = new URLClassPath(new URL[0]);
        URL url = ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/classes"));
        System.out.println("Local dir url: " + url);
        Object loader = ReflectionUtils.invoke("getLoader", ucp, url);
        System.out.println(loader.getClass());
        URL baseUrl = ReflectionUtils.invoke("getBaseURL", loader);
        System.out.println("Base url: " + baseUrl);
        URL var4 = new URL(baseUrl, ".");
        final URL var3 = new URL(baseUrl, ParseUtil.encodePath("bb/cc/dd/../../../../aa.txt", false));
        System.out.println("var4: " + var4 + ", " + var4.getFile());
        System.out.println("var3: " + var3 + ", " + var3.getFile());


        Resource res = ReflectionUtils.invoke("sun.misc.URLClassPath$FileLoader", "getResource",
                new Object[]{String.class, boolean.class}, loader, new Object[]{"aa.txt", false});
        System.out.println("Resource: " + res);
    }

    @Test
    public void testGetLoaderForURL() throws Exception {
        URLClassPath ucp = new URLClassPath(new URL[0]);

        URL url = new URL("http://localhost:8080/test/repository/classes/");
        System.out.println("Remote dir url: " + url);
        Object loader = ReflectionUtils.invoke("getLoader", ucp, url);
        System.out.println(loader.getClass());
        System.out.println("--------------------");

        url = ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/classes"));
        System.out.println("Local dir url: " + url);
        loader = ReflectionUtils.invoke("getLoader", ucp, url);
        System.out.println(loader.getClass());
        System.out.println("--------------------");

        url = ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/entity.jar"));
        System.out.println("Local jar url: " + url);
        loader = ReflectionUtils.invoke("getLoader", ucp, url);
        System.out.println(loader.getClass());
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
