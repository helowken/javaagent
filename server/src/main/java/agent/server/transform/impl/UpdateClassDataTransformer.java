package agent.server.transform.impl;

import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Optional;

public class UpdateClassDataTransformer extends AbstractTransformer {
    private final Map<Class<?>, byte[]> classToData;

    public UpdateClassDataTransformer(Map<Class<?>, byte[]> classToData) {
        this.classToData = classToData;
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        return Optional.ofNullable(
                classToData.get(classBeingRedefined)
        ).orElse(classfileBuffer);
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return true;
    }
}
