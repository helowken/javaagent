package agent.server.transform.impl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.function.Function;

public class UpdateClassDataTransformer implements ClassFileTransformer {
    private final Function<Class<?>, byte[]> classDataFunc;

    public UpdateClassDataTransformer(Function<Class<?>, byte[]> classDataFunc) {
        this.classDataFunc = classDataFunc;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return classDataFunc.apply(classBeingRedefined);
    }
}
