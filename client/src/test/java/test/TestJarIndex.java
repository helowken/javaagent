package test;

import org.junit.Test;
import sun.misc.JarIndex;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TestJarIndex {
    @Test
    public void test() throws Exception {
        String path = "/home/helowken/test_agent/common-lib/test/test.jar";
        JarFile jar = new JarFile(path);
        JarEntry indexEntry = jar.getJarEntry("META-INF/INDEX.LIST");
        new JarIndex(jar.getInputStream(indexEntry))
                .getJarFiles();
    }
}
