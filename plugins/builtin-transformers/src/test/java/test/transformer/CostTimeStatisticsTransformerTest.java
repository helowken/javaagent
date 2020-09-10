package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.CostTimeCallChainResultHandler;
import agent.builtin.tools.result.CostTimeInvokeResultHandler;
import agent.builtin.tools.result.parse.CostTimeCallChainResultParamParser;
import agent.builtin.tools.result.parse.CostTimeInvokeResultParamParser;
import agent.builtin.tools.result.parse.CostTimeResultParams;
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
                InvokeChainConfig.matchAllMethods()
        );
    }

    private void doTest(Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    CostTimeStatisticsTransformer transformer = new CostTimeStatisticsTransformer();
                    transformer.setInstanceKey(
                            newTransformerKey()
                    );

                    doTransform(transformer, config, classToMethodFilter, invokeChainConfig);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    Object a = newInstance(classToData, A.class);
                    ReflectionUtils.invoke("service", a);

                    flushAndWaitMetadata(outputPath);

                    CostTimeCallChainResultParamParser callChainOptParser = new CostTimeCallChainResultParamParser();
                    CostTimeResultParams params = callChainOptParser.parse(
                            new String[]{outputPath}
                    );
                    CostTimeCallChainResultHandler chainHandler = new CostTimeCallChainResultHandler();
                    chainHandler.exec(params);

                    System.out.println("\n======= Use cache 1 =======");
                    params = callChainOptParser.parse(
                            new String[]{"-m", "service", "-e", "avg > 20", "-cm", "runApi4", outputPath}
                    );
                    chainHandler.exec(params);

                    System.out.println("\n======= Use cache 2 =======");
                    params = callChainOptParser.parse(
                            new String[]{"-ce", "avg > 20", outputPath}
                    );
                    chainHandler.exec(params);

                    System.out.println("=========== For invoke =========\n");
                    CostTimeInvokeResultHandler invokeHandler = new CostTimeInvokeResultHandler();
                    CostTimeInvokeResultParamParser invokeParser = new CostTimeInvokeResultParamParser();
                    params = invokeParser.parse(
                            new String[]{"-m", "runApi*,service", outputPath}
                    );
                    invokeHandler.exec(params);

                    System.out.println("\n======= Use cache 4 =======");
                    params = invokeParser.parse(
                            new String[]{"-e", "avg > 20", outputPath}
                    );
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
