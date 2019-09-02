package test.server.classloader;

import agent.server.classloader.DynamicClassLoader;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class DynamicClassLoaderTest {
    @Test
    public void test() throws Exception {
        DynamicClassLoader loader = new DynamicClassLoader(null);
        loader.addURL(new URL("file:///home/helowken/projects/javaagent/test-rule/target/test-classes/"));
        assertNotNull(loader.loadClass("test.rule.TestRule", false));
    }

}
