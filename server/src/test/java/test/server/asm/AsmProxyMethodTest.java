package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static test.server.asm.AsmTestUtils.doCheck;

public class AsmProxyMethodTest {
    private final int count = 3;
    private static final String errorMsg = "xxxx";

    @Test
    public void testBeforeInnerCall() throws Exception {
        callWithoutArgs("beforeInnerCall");
    }

    @Test
    public void testArgsAndReturnWrapper() throws Exception {
        String methodName = "argsAndReturnWrapper";
        callWithArgs(methodName);
    }

    @Test
    public void testStaticArgsAndReturnWrapper() throws Exception {
        String methodName = "staticArgsAndReturnWrapper";
        callWithArgs(methodName);
    }

    @Test
    public void testThrowException() throws Exception {
        String methodName = "throwException";
        callError(methodName);
    }

    @Test
    public void testCallErrorFunc() throws Exception {
        String methodName = "callErrorFunc";
        callError(methodName);
    }

    @Test
    public void testTryFinally() throws Exception {
        String methodName = "tryFinally";
        callError(methodName);
    }

    @Test
    public void testTryCatch() throws Exception {
        callWithoutArgs("tryCatch");
    }

    @Test
    public void testTryCatchThrow() throws Exception {
        callError("tryCatchThrow");
    }

    @Test
    public void testTrySyncThrow() throws Exception {
        callError("trySyncThrow");
    }

    @Test
    public void trySyncMethodThrow() throws Exception {
        callError("trySyncMethodThrow");
    }

    @Test
    public void testTryNotCatch() throws Exception {
        callError("tryNotCatch");
    }

    @Test
    public void testReturnPrimitive() throws Exception {
        callWithoutArgs("returnPrimitive");
    }

    @Test
    public void testReturnVoid() throws Exception {
        callWithoutArgs("returnVoid");
    }

    private void callWithoutArgs(String methodName) throws Exception {
        List<String> logList = new ArrayList<>();
        Class<?> newAClass = AsmTestUtils.prepareClassMethod(count, logList, A.class, methodName);
        ReflectionUtils.invoke(
                newAClass,
                methodName,
                new Class[0],
                newAClass.newInstance()
        );
        doCheck(count, logList, false, true);
    }

    private void callError(String methodName) throws Exception {
        List<String> logList = new ArrayList<>();
        Class<?> newAClass = AsmTestUtils.prepareClassMethod(count, logList, A.class, methodName);
        try {
            ReflectionUtils.invoke(
                    newAClass,
                    methodName,
                    new Class[0],
                    newAClass.newInstance()
            );
            fail();
        } catch (Exception e) {
            Throwable t = Utils.getMeaningfulCause(e);
            t.printStackTrace();
            assertTrue(t instanceof RuntimeException);
            assertEquals(errorMsg, t.getMessage());
        }
        doCheck(count, logList, true, true);
    }

    private void callWithArgs(String methodName) throws Exception {
        List<String> logList = new ArrayList<>();
        Class<?> newAClass = AsmTestUtils.prepareClassMethod(count, logList, A.class, methodName);
        ReflectionUtils.invoke(
                newAClass,
                methodName,
                new Class[]{
                        int.class,
                        double.class,
                        String.class,
                        Short.class
                },
                newAClass.newInstance(),
                333,
                4.4,
                "sss",
                (short) 111
        );
        doCheck(count, logList, false, true);
    }

    public static class A {
        private B b = new B();

        private void beforeInnerCall() {
            System.out.println(
                    b.test(
                            (byte) 1,
                            (byte) 1,
                            (short) 2,
                            (short) 2,
                            3,
                            3,
                            4l,
                            4L,
                            true,
                            false,
                            6.1f,
                            6.1F,
                            7.2,
                            7.2,
                            "xxx",
                            new Object(),
                            new Date[]{
                                    new Date()
                            },
                            null
                    )
            );
        }

        private Double argsAndReturnWrapper(int a, double a2, String b, Short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("Return Double wrapper");
            return 3.3D;
        }

        private static String staticArgsAndReturnWrapper(int a, double a2, String b, Short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("Return Double wrapper");
            return "return from static";
        }

        private int returnPrimitive() {
            System.out.println("Return primitive");
            return 333;
        }

        private void returnVoid() {
            System.out.println("Return void");
        }

        private void throwException() {
            throw new RuntimeException(errorMsg);
        }

        private void callErrorFunc() {
            raiseError();
        }

        private void tryFinally() {
            try {
                throw new RuntimeException(errorMsg);
            } finally {
                System.out.println(111);
            }
        }

        private void tryCatch() {
            try {
                throw new Exception(errorMsg);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private void tryCatchThrow() {
            try {
                throw new Exception(errorMsg);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }

        private void tryNotCatch() throws Exception {
            try {
                throw new RuntimeException(errorMsg);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        private void trySyncThrow() throws Exception {
            synchronized (this) {
                throw new RuntimeException(errorMsg);
            }
        }

        private synchronized void trySyncMethodThrow() throws Exception {
            throw new RuntimeException(errorMsg);
        }

        private void raiseError() {
            throw new RuntimeException(errorMsg);
        }
    }

    public static class B {
        public long test(byte a1, Byte b1, short a2, Short b2, int a3, Integer b3, long a4, Long b4, boolean a5, Boolean b5,
                         float a6, Float b6, double a7, Double b7, String a8, Object a9, Date[] a10, Class<?> a11) {
            return 333;
        }
    }
}
