package test;

import sun.net.www.ParseUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class TestFileLoader {
    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = new URLClassLoader(
                new URL[]{
                        ParseUtil.fileToEncodedURL(new File("/home/helowken/test_loader/classes"))
                },
                null
        );
        Class<?> aClass = classLoader.loadClass("test.entity.A");
        System.out.println(aClass.getName());
    }
}
