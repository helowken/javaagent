package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.TraceResultParamParser;
import agent.builtin.tools.result.parse.TraceResultParams;
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
                    try {
                        ReflectionUtils.invoke(targetMethodName, a);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                    }

                    flushAndWaitMetadata(outputPath);

                    TraceInvokeResultHandler handler = new TraceInvokeResultHandler();

                    TraceResultParams params = new TraceResultParamParser().parse(
                            new String[]{"-s", "10000", outputPath}
                    );
                    handler.exec(params);

                    System.out.println("\n==============================");
                    params = new TraceResultParamParser().parse(
                            new String[]{"-o", "' '", outputPath}
                    );
                    handler.exec(params);
                }
        );
    }
}
