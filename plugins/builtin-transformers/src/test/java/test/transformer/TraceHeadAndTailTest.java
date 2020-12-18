package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.TraceResultParamParser;
import agent.builtin.tools.result.parse.TraceResultParams;
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
                            newTransformerKey()
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

                    TraceInvokeResultHandler handler = new TraceInvokeResultHandler();
                    TraceResultParams params;

                    System.out.println("\n=========== from head 0 ~ 4 ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-hn", "5", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n=========== from head 0 ~ 9 ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-hn", "15", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n=========== from tail 5 ~ 9 ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-tn", "5", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n=========== from tail 0 ~ 9 ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-tn", "11", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n=========== from head 0 ~ 2 && from tail 3 ~ 9 ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-hn", "3", "-tn", "7", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n=========== display all ===================");
                    params = new TraceResultParamParser().parse(
                            new String[]{outputPath}
                    );
                    handler.exec(params);
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
