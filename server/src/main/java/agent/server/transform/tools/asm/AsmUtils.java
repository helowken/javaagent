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
    public static void verifyAndPrintResult(byte[] bs, OutputStream out) {
        CheckClassAdapter.verify(
                new ClassReader(bs),
                true,
                new PrintWriter(out)
        );
    }

    public static String getVerifyResult(byte[] bs, boolean printResults) {
        StringWriter sw = new StringWriter();
        CheckClassAdapter.verify(
                new ClassReader(bs),
                printResults,
                new PrintWriter(sw)
        );
        return sw.toString();
    }

    public static void print(byte[] bs, OutputStream out) {
        new ClassReader(bs)
                .accept(
                        new TraceClassVisitor(
                                null,
                                new PrintWriter(out)
                        ),
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
