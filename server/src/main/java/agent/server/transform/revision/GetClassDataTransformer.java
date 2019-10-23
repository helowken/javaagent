package agent.server.transform.revision;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

class GetClassDataTransformer implements ClassFileTransformer {
    private Class<?> targetClass;
    private byte[] classData;

    void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        classData = null;
    }

    byte[] getClassData() {
        return classData;
    }

    void reset() {
        this.targetClass = null;
        this.classData = null;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined.equals(targetClass))
            this.classData = Arrays.copyOf(classfileBuffer, classfileBuffer.length);
        return classfileBuffer;
    }
}
