package agent.server.transform.impl;

import agent.server.transform.TransformResult;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class UpdateClassDataTransformer implements ClassFileTransformer {
    private final TransformResult result;

    public UpdateClassDataTransformer(TransformResult result) {
        this.result = result;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] data = result.getClassData(classBeingRedefined);
        if (data != null)
            return data;
        return classfileBuffer;
    }
}
