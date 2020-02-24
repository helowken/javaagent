package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.InvokeResultTracer;
import agent.builtin.transformer.TraceInvokeTransformer;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TraceInvokeTransformerTest extends AbstractTest {
    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    TraceInvokeTransformer transformer = new TraceInvokeTransformer();
                    String context = "test";
                    Map<Class<?>, String> classToMethodFilter = new HashMap<>();
                    classToMethodFilter.put(A.class, ".*");
                    doTransform(transformer, context, config, classToMethodFilter);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    Object a = newInstance(classToData, A.class);
                    ReflectionUtils.invoke("service", a);

                    flushAndWaitMetadata(outputPath);

                    InvokeResultTracer.main(
                            new String[]{
                                    outputPath
                            }
                    );
                }
        );
    }

    static class A {
        void service() {
            test(false, (byte) 2, 'a', (short) 4, 5, (long) 6);
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
            return "xxx";
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
