package test.server.utils;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.lang.instrument.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public class TestInstrumentation implements Instrumentation {
    private List<ClassFileTransformer> transformerList = new ArrayList<>();
    private Map<String, byte[]> classNameToBytes = new HashMap<>();

    public byte[] getBytes(String className) {
        return classNameToBytes.get(className);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        transformerList.add(transformer);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        transformerList.add(transformer);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return transformerList.remove(transformer);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return false;
    }

    private byte[] getClassData(Class<?> clazz) {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        byte[] classData = null;
        if (codeSource != null) {
            URL url = codeSource.getLocation();
            if (url != null) {
                classData = Utils.wrapToRtError(() -> {
                    URLClassPath ucp = new URLClassPath(
                            new URL[]{url}
                    );
                    Resource resource = ucp.getResource(
                            ReflectionUtils.getClassNamePath(clazz) + ".class"
                    );
                    if (resource != null)
                        return resource.getBytes();
                    return null;
                });
            }
        }
        return classData;
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        for (Class<?> clazz : classes) {
            for (ClassFileTransformer transformer : transformerList) {
                try {
                    byte[] bs = transformer.transform(
                            clazz.getClassLoader(),
                            clazz.getName().replaceAll("\\.", "/"),
                            clazz,
                            clazz.getProtectionDomain(),
                            getClassData(clazz)
                    );
                    classNameToBytes.put(clazz.getName(), bs);
                } catch (IllegalClassFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return false;
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {

    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return false;
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return new Class[0];
    }

    @Override
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return new Class[0];
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return 0;
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {

    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {

    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return false;
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {

    }
}
