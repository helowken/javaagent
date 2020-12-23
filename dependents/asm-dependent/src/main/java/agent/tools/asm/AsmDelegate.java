package agent.tools.asm;

import agent.invoke.DestInvoke;
import agent.invoke.data.ClassInvokeItem;
import agent.invoke.data.TypeItem;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class AsmDelegate {
    public static ClassInvokeItem collect(byte[] classData) {
        ClassReader cr = new ClassReader(classData);
        CollectInvokeClassVisitor cv = new CollectInvokeClassVisitor();
        cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cv.getClassInvokeItem();
    }

    public static String getInvokeFullName(String invokeName, String invokeDesc) {
        StringBuilder sb = new StringBuilder();
        Type invokeType = Type.getMethodType(invokeDesc);
        sb.append(invokeName).append('(');
        Type[] argTypes = invokeType.getArgumentTypes();
        if (argTypes != null) {
            int count = 0;
            for (Type argType : argTypes) {
                if (count > 0)
                    sb.append(",");
                sb.append(argType.getClassName());
                ++count;
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static TypeItem parseType(String invokeOwner) {
        Type type = Type.getObjectType(invokeOwner);
        return type.getSort() == Type.ARRAY ?
                new TypeItem(
                        type.getElementType().getClassName(),
                        true,
                        type.getDimensions()
                ) :
                new TypeItem(
                        type.getClassName()
                );
    }

    public static void verifyAndPrintResult(ClassLoader loader, byte[] bs, OutputStream out) {
        verify(
                loader,
                bs,
                true,
                new PrintWriter(System.out)
        );
    }

    public static String getVerifyResult(ClassLoader loader, byte[] bs, boolean printResults) {
        StringWriter sw = new StringWriter();
        verify(
                loader,
                bs,
                printResults,
                new PrintWriter(sw)
        );
        return sw.toString();
    }

    private static void verify(ClassLoader loader, byte[] bs, boolean printResults, PrintWriter pw) {
        CheckClassAdapter.verify(
                new ClassReader(bs),
                loader,
                printResults,
                pw
        );
    }

    public static String convertToReadableContent(byte[] bs) {
        StringWriter sw = new StringWriter();
        printClass(
                bs,
                new PrintWriter(sw)
        );
        return sw.toString();
    }

    public static void print(byte[] bs, OutputStream out) {
        printClass(
                bs,
                new PrintWriter(out)
        );
    }

    private static void printClass(byte[] bs, PrintWriter pw) {
        new ClassReader(bs)
                .accept(
                        new TraceClassVisitor(null, pw),
                        0
                );
    }

    public static ClassNode newClassNode(byte[] bs) {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(bs);
        cr.accept(cn, 0);
        return cn;
    }

    private static ClassNode transform(byte[] bs, TransformFunc transformFunc) {
        ClassNode cn = newClassNode(bs);
        transformFunc.transform(cn);
        return cn;
    }

    private static byte[] transformClass(Class<?> sourceClass, byte[] bs, TransformFunc transformFunc) {
        ClassNode cn = transform(bs, transformFunc);
        ClassWriter cw = new AsmClassWriter(
                ClassWriter.COMPUTE_FRAMES,
                sourceClass.getClassLoader()
        );
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] transform(Class<?> sourceClass, byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
        return transformClass(
                sourceClass,
                classData,
                classNode -> AsmTransformProxy.doTransform(classNode, idToInvoke)
        );
    }

    interface TransformFunc {
        void transform(ClassNode classNode);
    }
}
