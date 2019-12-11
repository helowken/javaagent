package test.server.asm;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.server.transform.tools.asm.*;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;

public class AsmTest {

    @Test
    public void test() throws Exception {
        byte[] bs = IOUtils.readBytes(
                ClassLoader.getSystemResourceAsStream(A.class.getName().replace('.', '/') + ".class")
        );
        Method destMethod = ReflectionUtils.findFirstMethod(A.class, "test");
        ProxyRegInfo regInfo = new ProxyRegInfo(destMethod);
        for (int i = 0; i < 3; ++i) {
            B b = new B();
            regInfo.addBefore(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(B.class, "testBefore"),
                            DEFAULT_BEFORE
                    )
            ).addAfter(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(B.class, "testAfter"),
                            DEFAULT_AFTER
                    )
            ).addAround(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(B.class, "testAround"),
                            DEFAULT_AROUND
                    )
            ).addAfterReturning(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(B.class, "testAfterReturning"),
                            DEFAULT_AFTER_RETURNING
                    )
            ).addAfterThrowing(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(B.class, "testAfterThrowing"),
                            DEFAULT_AFTER_THROWING
                    )
            );
        }

        ProxyTransformMgr.ProxyResult item = ProxyTransformMgr.getInstance().transform(
                bs,
                Collections.singleton(regInfo)
        );

        byte[] classData = item.getClassData();
        if (classData == null)
            return;

        AsmUtils.verifyAndPrintResult(classData);
        System.out.println("=========================\n");

        AsmUtils.print(classData);
        System.out.println("=========================\n");

        ProxyTransformMgr.getInstance().reg(item);

        Class<?> newAClass = new TestClassLoader().loadClass(A.class.getName(), classData);
        ReflectionUtils.invoke(
                newAClass,
                "test",
                new Class[]{
                        int.class,
                        String.class,
                        short.class
                },
                newAClass.newInstance(),
                333,
                "sss",
                (short) 111
        );
        System.out.println("========================");
        ReflectionUtils.invoke(
                newAClass,
                "test",
                new Class[]{
                        int.class,
                        String.class,
                        short.class
                },
                newAClass.newInstance(),
                1,
                "sss",
                (short) 111
        );
    }

    private static class TestClassLoader extends ClassLoader {
        Class<?> loadClass(String className, byte[] bs) {
            return super.defineClass(className, bs, 0, bs.length);
        }
    }

    public static class A {
        public double test(int a, String b, short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("DDD: " + null);
            if (a == 1)
                raiseError();
            return 3.3D;
        }

        private void raiseError() {
            throw new RuntimeException("xxx");
        }
    }

    public static class B {
        private static int idx;

        private final int count;

        B() {
            this.count = idx++;
        }

        public void testBefore(Object[] args, Class<?>[] argTypes) {
            System.out.println(
                    "==== test before: " + count +
                            ", Args: " + Arrays.toString(args) +
                            ", Arg Types: " + Arrays.toString(argTypes)
            );
        }

        public void testAfter(Object returnValue, Class<?> returnType, Throwable error) {
            System.out.println(
                    "==== test after: " + count +
                            (error != null ?
                                    ", Error: " + error :
                                    ", Return Value: " + returnValue + ", Return Type: " + returnType
                            )
            );
        }

        public void testAround(ProxyCallChain callChain) {
            System.out.println("==== test around start: " + count);
            callChain.process();
            System.out.println("==== test around end: " + count);
        }

        public void testAfterReturning(Object returnValue, Class<?> returnType) {
            System.out.println(
                    "==== test after returning: " + count +
                            ", Return Value: " + returnValue +
                            ", Return Type: " + returnType
            );
        }

        public void testAfterThrowing(Throwable error) {
            System.out.println(
                    "==== test after throwing: " + count +
                            ", Error: " + error
            );
        }
    }
}
