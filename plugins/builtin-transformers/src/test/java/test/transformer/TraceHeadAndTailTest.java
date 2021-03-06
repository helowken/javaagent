package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.ResultLauncher;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformerRegistry;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.HashMap;
import java.util.Map;

public class TraceHeadAndTailTest extends AbstractTest {
    private static final Map<Class<?>, byte[]> classToData = new HashMap<>();

    @Test
    public void test() throws Exception {
        Class<?> targetClass = TestRun.class;
        String targetMethodName = "run";
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(targetClass, targetMethodName);
        InvokeChainConfig invokeChainConfig = InvokeChainConfig.matchAll("test.*", "test.*");

        runWithFile(
                (outputPath, config) -> {
                    ConfigTransformer transformer = TransformerRegistry.getOrCreateTransformer(
                            TraceInvokeTransformer.REG_KEY,
                            newTransformerKey(),
                            null
                    );
                    doTransform(transformer, config, classToMethodFilter, invokeChainConfig);

                    classToData.putAll(
                            getClassToData(transformer)
                    );
                    Map<String, Class<?>> nameToClass = loadNewClasses(classToData);
                    Object a = nameToClass.get(
                            targetClass.getName()
                    ).newInstance();
                    for (int i = 0; i < 10; ++i) {
                        try {
                            ReflectionUtils.invoke(targetMethodName, a);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }
                    }

                    flushAndWaitMetadata(transformer.getTid());
                    System.out.println("\n=========== from head 0 ~ 4 ===================");
                    ResultLauncher.main(
                            new String[]{"tr", "-hn", "5", outputPath}
                    );

                    System.out.println("\n=========== from head 0 ~ 9 ===================");
                    ResultLauncher.main(
                            new String[]{"tr", "-hn", "15", outputPath}
                    );

                    System.out.println("\n=========== from tail 5 ~ 9 ===================");
                    ResultLauncher.main(
                            new String[]{"tr", "-tn", "5", outputPath}
                    );

                    System.out.println("\n=========== from tail 0 ~ 9 ===================");
                    ResultLauncher.main(
                            new String[]{"tr", "-tn", "11", outputPath}
                    );

                    System.out.println("\n=========== from head 0 ~ 2 && from tail 3 ~ 9 ===================");
                    ResultLauncher.main(
                            new String[]{"tr", "-hn", "3", "-tn", "7", outputPath}
                    );

                    System.out.println("\n=========== display all ===================");
                    ResultLauncher.main(
                            new String[]{"tr", outputPath}
                    );
                }
        );
    }

    public static class TestRun {
        private int times = 0;

        public int run() {
            return times++;
        }
    }
}
