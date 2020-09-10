package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.TraceResultParamParser;
import agent.builtin.tools.result.parse.TraceResultParams;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.tools.asm.AsmUtils;
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
                InvokeChainConfig.matchAll(),
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
                    TraceInvokeTransformer transformer = new TraceInvokeTransformer();
                    transformer.setInstanceKey(
                            newTransformerKey()
                    );
                    doTransform(transformer, config, classToMethodFilter, invokeChainConfig);

                    classToData.putAll(
                            getClassToData(transformer)
                    );
                    Map<String, Class<?>> nameToClass = new HashMap<>();
                    classToData.forEach(
                            (clazz, data) -> Utils.wrapToRtError(
                                    () -> {
                                        try {
                                            nameToClass.put(
                                                    clazz.getName(),
                                                    loader.loadClass(
                                                            clazz.getName(),
                                                            data
                                                    )
                                            );
                                            System.out.println("========================");
                                            AsmUtils.print(data, System.out);
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                            )
                    );
                    Object a = nameToClass.get(
                            targetClass.getName()
                    ).newInstance();
                    try {
                        ReflectionUtils.invoke(targetMethodName, a);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                    }

                    flushAndWaitMetadata(outputPath);
                    System.out.println(IOUtils.readToString(outputPath));

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
