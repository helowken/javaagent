package test.server;

import agent.base.utils.ClassLoaderUtils;
import org.junit.Test;

public class TestClassLoader {
    @Test
    public void test() throws Exception {
        String className = "com.fasterxml.jackson.core.type.TypeReference";
        ClassLoader loader = ClassLoaderUtils.initContextClassLoader("/home/helowken/test_agent/server/../common-lib/dependent-lib.jar");
        System.out.println(loader.loadClass(className));
        System.out.println(Class.forName(className));
    }
}
