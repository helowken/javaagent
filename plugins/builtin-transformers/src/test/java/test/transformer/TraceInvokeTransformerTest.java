package test.transformer;

import agent.common.config.InvokeChainConfig;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TraceInvokeTransformerTest extends AbstractTraceTest {

    @Test
    public void test() throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(A.class, "*");
        doTest(
                A.class,
                "service",
                classToMethodFilter,
                null,
                true
        );
    }

    @Test
    public void test2() throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(A.class, "service");
        doTest(
                A.class,
                "service",
                classToMethodFilter,
                InvokeChainConfig.matchAll(),
                true
        );
    }

    public static class A {
        void service() {
            test(false, (byte) 2, '\'', (short) 4, 5, (long) 6);
        }

        String test(boolean a1, byte a2, char a3, short a4, int a5, long a6) {
            System.out.println(
                    test2(7.1f, 8.2)
            );
            test3(
                    new B()
            );
            try {
                test6();
            } catch (Exception e) {
            }
            return "xxxxxxxxxxyyyyyyyyyyxxxxxxxxxxyyyyyyyyyyxxxxxxxxxxyyyyyyyyyy";
        }

        Date test2(float a7, double a8) {
            return new Date();
        }

        C test3(B b) {
            test4(true, (byte) 2, 'A', (short) 4, 5, 6L);
            return new C();
        }

        Double test4(Boolean a1, Byte a2, Character a3, Short a4, Integer a5, Long a6) {
            for (int i = 0; i < 3; ++i) {
                test5(i + 7.2F, 8.3);
            }
            return 99.9;
        }

        short test5(Float a7, Double a8) {
            return 555;
        }

        void test6() {
            throw new IllegalArgumentException("Argument Error!!!");
        }
    }

    public static class B {
    }

    public static class C {
    }
}
