package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.ResultLauncher;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformerRegistry;
import test.server.AbstractTest;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractTraceTest extends AbstractTest {
    private static final Map<Class<?>, byte[]> classToData = new HashMap<>();


    void test(Class<?> clazz, String methodName, boolean runFirst) throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(clazz, methodName);
        doTest(
                clazz,
                methodName,
                classToMethodFilter,
                InvokeChainConfig.matchAll("test.*", "test.*"),
                runFirst
        );
    }

    void doTest(Class<?> targetClass, String targetMethodName, Map<Class<?>, String> classToMethodFilter,
                InvokeChainConfig invokeChainConfig, boolean runFirst) throws Exception {
        if (runFirst) {
            try {
                ReflectionUtils.invoke(
                        targetMethodName,
                        targetClass.newInstance()
                );
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
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
                    try {
                        ReflectionUtils.invoke(targetMethodName, a);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                    }

                    flushAndWaitMetadata(transformer.getTid());

                    ResultLauncher.main(
                            new String[]{"tr", "-s", "10000", outputPath}
                    );
                    System.out.println("\n==============================");
                    ResultLauncher.main(
                            new String[]{"tr", "-o", "' '", outputPath}
                    );
                }
        );
    }
}
