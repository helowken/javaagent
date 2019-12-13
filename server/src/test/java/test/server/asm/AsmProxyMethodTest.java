package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
        assertEquals(
                newExpectedList(count, true),
                logList
        );
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

        assertEquals(
                newExpectedList(count, false),
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
        public double test(int a, String b, short ccc) {
            System.out.println("a: " + a + ", b: " + b + ", ccc: " + ccc);
            System.out.println("DDD: " + null);
            if (a == 1)
                raiseError();
            return 3.3D;
        }

        private void raiseError() {
            throw new RuntimeException(errorMsg);
        }
    }
}
