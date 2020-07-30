package agent.server.transform.search.invoke;

import org.objectweb.asm.ClassReader;

public class ClassInvokeCollector {
    public static ClassInvokeItem collect(byte[] classData) {
        ClassReader cr = new ClassReader(classData);
        CollectInvokeClassVisitor cv = new CollectInvokeClassVisitor();
        cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cv.getClassInvokeItem();
    }
}
