package test.server.classloader;

import agent.server.classloader.DynamicClassLoader;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class DynamicClassLoaderTest {
    @Test
    public void test() throws Exception {
        Class<?> clazz = DynamicRule.class;
        DynamicClassLoader loader = new DynamicClassLoader(null);
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        loader.addURL(url);
        assertNotNull(loader.loadClass(clazz.getName(), false));
    }

    private static class DynamicRule {
    }
}
