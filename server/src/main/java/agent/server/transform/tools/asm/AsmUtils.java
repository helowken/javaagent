package agent.server.transform.tools.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AsmUtils {
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

    public static byte[] transform(byte[] bs, TransformFunc transformFunc) {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(bs);
        cr.accept(cn, 0);

        transformFunc.transform(cn);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    interface TransformFunc {
        void transform(ClassNode classNode);
    }
}
