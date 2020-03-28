package agent.server.transform.revision;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

class GetClassDataTransformer implements ClassFileTransformer {
    private final Class<?> targetClass;
    private byte[] classData;

    GetClassDataTransformer(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    byte[] getData() {
        return classData;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined.equals(targetClass))
            this.classData = Arrays.copyOf(classfileBuffer, classfileBuffer.length);
        return null;
    }
}
