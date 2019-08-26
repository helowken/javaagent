package test;

import agent.base.utils.ReflectionUtils;
import sun.misc.Launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class TestURLClassLoader {
    public static void main(String[] args) throws Exception {
        String className = "agent.builtin.BuiltinTransformerPlugin";
        URL url = new File("/home/helowken/test_agent/server/plugin/builtin-transformers-1.0.jar").toURI().toURL();

        System.out.println(new File("/home/helowken/test_agent/server/plugin/../../server/plugin").getAbsolutePath());
        System.out.println(new File("/home/helowken/test_agent/server/plugin/../../server/plugin").getCanonicalPath());

        new URLClassLoader(
                new URL[]{url},
                null
        )
                .loadClass(className);

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
            System.out.println(appClassLoader);
        }
//
//        ClassLoader classLoader = ReflectionUtils.invokeStatic(
//                "sun.misc.Launcher$ExtClassLoader",
//                "createExtClassLoader"
//        );
//        System.out.println(classLoader);
    }
}
