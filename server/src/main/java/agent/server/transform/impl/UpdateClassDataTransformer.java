package agent.server.transform.impl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

public class UpdateClassDataTransformer implements ClassFileTransformer {
    private final Map<Class<?>, byte[]> classToData;

    public UpdateClassDataTransformer(Map<Class<?>, byte[]> classToData) {
        this.classToData = classToData;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] data = classToData.get(classBeingRedefined);
        if (data != null)
            return data;
        return classfileBuffer;
    }
}
