package test.utils;

import java.lang.instrument.*;
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
                            new byte[0]
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
