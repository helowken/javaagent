package test.server;

import agent.base.utils.IOUtils;
import agent.base.utils.Utils;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class TestInstrumentation implements Instrumentation {
    private List<ClassFileTransformer> transformerList = new ArrayList<>();

    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        transformerList.add(transformer);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
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
        if (classes == null)
            return;
        for (Class<?> clazz : classes) {
            for (ClassFileTransformer transformer : transformerList) {
                Utils.wrapToRtError(
                        () -> transformer.transform(
                                clazz.getClassLoader(),
                                clazz.getName(),
                                clazz,
                                clazz.getProtectionDomain(),
                                IOUtils.readBytes(
                                        ClassLoader.getSystemResourceAsStream(clazz.getName().replace('.', '/') + ".class")
                                )
                        )
                );
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
