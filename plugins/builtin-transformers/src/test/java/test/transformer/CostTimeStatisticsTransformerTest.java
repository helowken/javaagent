package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.ByCallChainCostTimeResultHandler;
import agent.builtin.tools.result.ByInvokeCostTimeResultHandler;
import agent.builtin.tools.result.CostTimeResultOptions;
import agent.builtin.tools.result.CostTimeResultParams;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.common.config.InvokeChainConfig;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.HashMap;
import java.util.Map;

public class CostTimeStatisticsTransformerTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(A.class, "*");
        doTest(classToMethodFilter, null);
    }

    @Test
    public void test2() throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(A.class, "service");
        doTest(
                classToMethodFilter,
                new InvokeChainConfig()
        );
    }

    private void doTest(Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    CostTimeStatisticsTransformer transformer = new CostTimeStatisticsTransformer();
                    transformer.setInstanceKey(
                            newTransformerKey()
                    );

                    String context = "test";
                    doTransform(transformer, context, config, classToMethodFilter, invokeChainConfig);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    Object a = newInstance(classToData, A.class);
                    ReflectionUtils.invoke("service", a);

                    flushAndWaitMetadata(outputPath);

                    CostTimeResultOptions opts = new CostTimeResultOptions();
                    CostTimeResultParams params = new CostTimeResultParams();
                    params.inputPath = outputPath;
                    params.opts = opts;
                    ByCallChainCostTimeResultHandler chainHandler = new ByCallChainCostTimeResultHandler();
                    chainHandler.exec(params);

                    System.out.println("\n======= Use cache =======");
                    opts = new CostTimeResultOptions();
                    opts.methodStr = "runApi4:service";
                    opts.filterExpr = "avgTime > 20";
                    params.opts = opts;
                    chainHandler.exec(params);

                    System.out.println("====================\n");
                    ByInvokeCostTimeResultHandler invokeHandler = new ByInvokeCostTimeResultHandler();
                    opts = new CostTimeResultOptions();
                    opts.methodStr = "runApi*:service";
                    params.opts = opts;
                    invokeHandler.exec(params);

                    System.out.println("\n======= Use cache =======");
                    opts = new CostTimeResultOptions();
                    opts.filterExpr = "avgTime > 20";
                    params.opts = opts;
                    invokeHandler.exec(params);
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
