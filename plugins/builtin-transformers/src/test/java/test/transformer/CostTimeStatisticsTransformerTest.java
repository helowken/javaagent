package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.CostTimeByCallChain;
import agent.builtin.tools.CostTimeByInvoke;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.HashMap;
import java.util.Map;

public class CostTimeStatisticsTransformerTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    CostTimeStatisticsTransformer transformer = new CostTimeStatisticsTransformer();

                    String context = "test";
                    Map<Class<?>, String> classToMethodFilter = new HashMap<>();
                    classToMethodFilter.put(A.class, ".*");
                    doTransform(transformer, context, config, classToMethodFilter);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    Object a = newInstance(classToData, A.class);
                    ReflectionUtils.invoke("service", a);

                    flushAndWaitMetadata(outputPath);

                    CostTimeByCallChain.main(
                            new String[]{
                                    outputPath
                            }
                    );

                    System.out.println("====================");

                    CostTimeByInvoke.main(
                            new String[] {
                                    outputPath
                            }
                    );
                }
        );
    }

    static class A {
        void service() {
            try {
                System.out.println("Running in service");
                runApi1();
                runApi2();
                runApi2();
                for (int i = 0; i < 3; ++i) {
                    runApi3();
                }
                runApi4(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void runApi1() throws InterruptedException {
            System.out.println("Running in api1");
            Thread.sleep(10);
            commonCalls(1);
        }

        private void runApi2() throws InterruptedException {
            System.out.println("Running in api2");
            Thread.sleep(15);
            runApi2_1();
            commonCalls(2);
        }

        private void runApi2_1() throws InterruptedException {
            System.out.println("Running in api2_1");
            Thread.sleep(20);
            commonCall();
        }

        private void runApi3() throws InterruptedException {
            System.out.println("Running in api3");
            Thread.sleep(25);
            runApi3_1();
            commonCalls(3);
        }

        private void runApi3_1() throws InterruptedException {
            System.out.println("Running in api3_1");
            Thread.sleep(30);
            int i = 0;
            while (i++ < 3) {
                runApi3_1_1();
                commonCall();
            }
        }

        private void runApi3_1_1() throws InterruptedException {
            System.out.println("Running in api3_1_1");
            Thread.sleep(35);
            commonCall();
        }

        private void runApi4(int n) throws InterruptedException {
            if (n <= 0)
                return;
            System.out.println("Running in api4 on n=" + n);
            Thread.sleep(5);
            commonCall();
            runApi4(n - 1);
        }

        private void commonCalls(Integer count) throws InterruptedException {
            for (int i = 0; i < count; ++i) {
                commonCall();
            }
        }

        private void commonCall() throws InterruptedException {
            System.out.println("Running in commonCall");
            Thread.sleep(5);
        }
    }
}