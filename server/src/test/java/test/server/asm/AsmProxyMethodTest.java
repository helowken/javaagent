package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AsmProxyMethodTest {
    final int count = 3;
    private static final String errorMsg = "xxxx";

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
        doCheck(count, logList, false);
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
        doCheck(count, logList, true);
    }

    private void callWithArgs(String methodName) throws Exception {
        List<String> logList = new ArrayList<>();
        Class<?> newAClass = AsmTestUtils.prepareClassMethod(count, logList, A.class, methodName);
        ReflectionUtils.invoke(
                newAClass,
                methodName,
                new Class[]{
                        int.class,
                        String.class,
                        Short.class
                },
                newAClass.newInstance(),
                333,
                "sss",
                (short) 111
        );
        doCheck(count, logList, false);
    }

    private void doCheck(int count, List<String> logList, boolean throwError) {
        assertEquals(
                newExpectedList(count, throwError),
                logList
        );
    }

    private static List<String> newExpectedList(int count, boolean throwError) {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("before");
        prefixList.add(throwError ? "onThrowing" : "onReturning");
        prefixList.add("after");

        List<String> expectedList = new ArrayList<>();
        for (String prefix : prefixList) {
            for (int i = 0; i < count; ++i) {
                expectedList.add(prefix + "-" + i);
            }
        }
        return expectedList;
    }

    public static class A {
        private Double argsAndReturnWrapper(int a, String b, Short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("Return Double wrapper");
            return 3.3D;
        }

        private static String staticArgsAndReturnWrapper(int a, String b, Short ccc) {
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
}
