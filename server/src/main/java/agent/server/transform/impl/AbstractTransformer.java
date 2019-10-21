package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.server.transform.ErrorTraceTransformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractTransformer implements ErrorTraceTransformer {
    private static final Logger logger = Logger.getLogger(AbstractTransformer.class);
    private Throwable error;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (accept(loader, className)) {
            String targetClassName = TransformerInfo.getClassName(className);
            logger.debug("Transforming class: {}, class loader: {}", targetClassName, loader);
            try {
                byte[] bs = doTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, targetClassName);
                postTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, bs);
                return bs;
            } catch (Throwable e) {
                this.error = e;
            }
        }
        return classfileBuffer;
    }

    protected void postTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                 ProtectionDomain protectionDomain, byte[] classfileBuffer, byte[] newBuffer) throws Exception {
    }

    @Override
    public Set<Class<?>> getRefClassSet() {
        return Collections.singleton(getClass());
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    protected abstract byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                          ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception;

    protected abstract boolean accept(ClassLoader loader, String namePath);
}
