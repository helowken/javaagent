package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.tools.asm.ProxyRegInfo;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AsmProxyMethodTest {
    private static final String errorMsg = "xxxx";

    @Test
    public void test() throws Exception {
        final int count = 3;
        List<String> logList = new ArrayList<>();

        Method destMethod = ReflectionUtils.findFirstMethod(A.class, "test");
        ProxyRegInfo regInfo = new ProxyRegInfo(destMethod);
        Class<?> newAClass = AsmTestUtils.prepareClass(count, logList, regInfo);

        callNormal(newAClass, count, logList);
        System.out.println("========================");
        logList.clear();
        callError(newAClass, count, logList);
    }

    @Test
    public void test2() throws Exception {
        doTestNoArgsMethod("test2");
    }

    @Test
    public void test3() throws Exception {
        doTestNoArgsMethod("test3");
    }

    private void doTestNoArgsMethod(String methodName) throws Exception {
        final int count = 3;
        List<String> logList = new ArrayList<>();

        Method destMethod = ReflectionUtils.findFirstMethod(A.class, methodName);
        ProxyRegInfo regInfo = new ProxyRegInfo(destMethod);
        Class<?> newAClass = AsmTestUtils.prepareClass(count, logList, regInfo);

        ReflectionUtils.invoke(
                newAClass,
                methodName,
                new Class[0],
                newAClass.newInstance()
        );
        doCheck(count, logList, false);
    }

    private void callError(Class<?> newAClass, int count, List<String> logList) {
        try {
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
            fail();
        } catch (Exception e) {
            Throwable t = Utils.getMeaningfulCause(e);
            assertTrue(t instanceof RuntimeException);
            assertEquals(errorMsg, t.getMessage());
        }
        doCheck(count, logList, true);
    }

    private void callNormal(Class<?> newAClass, int count, List<String> logList) throws Exception {
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
        doCheck(count, logList, false);
    }

    private void doCheck(int count, List<String> logList, boolean throwError) {
        assertEquals(
                newExpectedList(count, throwError),
                logList
        );
    }

    private static List<String> newExpectedList(int count, boolean throwError) {
        String[] prefixList = new String[]{
                "before", "aroundStart"
        };
        List<String> expectedList = new ArrayList<>();
        for (String prefix : prefixList) {
            for (int i = 0; i < count; ++i) {
                expectedList.add(prefix + "-" + i);
            }
        }
        for (int i = count - 1; i >= 0; --i) {
            expectedList.add("aroundEnd-" + i);
        }
        prefixList = new String[]{
                throwError ? "afterThrowing" : "afterReturning",
                "after"
        };
        for (String prefix : prefixList) {
            for (int i = 0; i < count; ++i) {
                expectedList.add(prefix + "-" + i);
            }
        }
        return expectedList;
    }

    public static class A {
        public Double test(int a, String b, short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("Return Double wrapper");
            if (a == 1)
                raiseError();
            return 3.3D;
        }

        public int test2() {
            System.out.println("Return int primitive");
            return 333;
        }

        public void test3() {
            System.out.println("Return void");
        }

        private void raiseError() {
            throw new RuntimeException(errorMsg);
        }
    }
}
