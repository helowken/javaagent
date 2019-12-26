package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.CostTimeByCallChain;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.server.transform.impl.DestInvokeIdRegistry;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CostTimeStatisticsTransformerTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        Path path = Files.createTempFile("costTime-", ".log");
        File logFile = path.toFile();
        String outputPath = logFile.getAbsolutePath();
        try {
            CostTimeStatisticsTransformer transformer = new CostTimeStatisticsTransformer();
            Map<String, Object> logConf = new HashMap<>();
            logConf.put("outputPath", outputPath);

            String context = "test";
            Map<Class<?>, String> classToMethodFilter = new HashMap<>();
            classToMethodFilter.put(A.class, ".*");
            doTransform(transformer, context, Collections.singletonMap("log", logConf), classToMethodFilter);

            Map<Class<?>, byte[]> classToData = getClassToData(transformer);

            Object a = newInstance(classToData, A.class);
            ReflectionUtils.invoke("service", a);

            flushAndWaitMetadata();

            CostTimeByCallChain.main(
                    new String[]{
                            outputPath
                    }
            );
        } finally {
            Files.delete(path);
            new File(outputPath + DestInvokeIdRegistry.METADATA_FILE).delete();
        }
    }

    static class A {
        void service() {
            try {
                System.out.println("Running in service");
                runApi1();
                runApi2();
                runApi2();
                runApi3();
                runApi3();
                runApi3();
//                for (int i = 0; i < 1000; ++i) {
//                    runApi3();
//                }
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
            runApi3_1_1();
            commonCall();
        }

        private void runApi3_1_1() throws InterruptedException {
            System.out.println("Running in api3_1_1");
            Thread.sleep(35);
            commonCall();
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
