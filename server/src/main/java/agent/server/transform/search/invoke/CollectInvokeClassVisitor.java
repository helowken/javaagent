package agent.server.transform.search.invoke;

import agent.base.utils.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM7;

class CollectInvokeClassVisitor extends ClassVisitor {
    private static final Logger logger = Logger.getLogger(CollectInvokeClassVisitor.class);
    private ClassInvokeItem classInvokeItem = new ClassInvokeItem();

    CollectInvokeClassVisitor() {
        super(ASM7);
    }

    ClassInvokeItem getClassInvokeItem() {
        return classInvokeItem;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        return new CollectInvokeMethodVisitor(name, descriptor);
    }

    private class CollectInvokeMethodVisitor extends MethodVisitor {
        private final String invokeName;
        private final String invokeDesc;
        private final List<InnerInvokeItem> invokeItems = new ArrayList<>();

        private CollectInvokeMethodVisitor(String name, String desc) {
            super(ASM7);
            this.invokeName = name;
            this.invokeDesc = desc;
        }

        @Override
        public void visitCode() {
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            invokeItems.add(
                    new InnerInvokeItem(owner, name, descriptor, false)
            );
        }

        @Override
        public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapHandle, final Object... bootstrapMethodArguments) {
            if (bootstrapMethodArguments == null)
                logger.warn("bootstrapMethodArguments is null.");
            else if (bootstrapMethodArguments.length < 2)
                logger.warn("bootstrapMethodArguments length is: {}", bootstrapMethodArguments.length);
            else {
                Handle handle = (Handle) bootstrapMethodArguments[1];
                invokeItems.add(
                        new InnerInvokeItem(
                                handle.getOwner(),
                                handle.getName(),
                                handle.getDesc(),
                                true
                        )
                );
            }
        }

        @Override
        public void visitEnd() {
            classInvokeItem.add(invokeName, invokeDesc, invokeItems);
        }
    }
}
