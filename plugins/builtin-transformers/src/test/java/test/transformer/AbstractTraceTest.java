package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.TraceResultOptions;
import agent.builtin.tools.result.TraceResultParams;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.InvokeChainConfig;
import test.server.AbstractTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractTraceTest extends AbstractTest {
    private static final Map<Class<?>, byte[]> classToData = new HashMap<>();

    void test(Class<?> clazz, String methodName, boolean runFirst) throws Exception {
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(clazz, methodName);

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        ConstructorFilterConfig constructorFilterConfig = new ConstructorFilterConfig();
        constructorFilterConfig.setIncludes(
                Collections.singleton("*")
        );
        invokeChainConfig.setMatchConstructorFilter(constructorFilterConfig);
        doTest(clazz, methodName, classToMethodFilter, invokeChainConfig, runFirst);
    }

    void doTest(Class<?> targetClass, String targetMethodName, Map<Class<?>, String> classToMethodFilter,
                InvokeChainConfig invokeChainConfig, boolean runFirst) throws Exception {
        if (runFirst)
            ReflectionUtils.invoke(
                    targetMethodName,
                    targetClass.newInstance()
            );
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
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                            )
                    );
                    Object a = nameToClass.get(
                            targetClass.getName()
                    ).newInstance();
                    ReflectionUtils.invoke(targetMethodName, a);

                    flushAndWaitMetadata(outputPath);
                    System.out.println(IOUtils.readToString(outputPath));

                    TraceResultParams params = new TraceResultParams();
                    params.inputPath = outputPath;
                    TraceResultOptions opts = new TraceResultOptions();
                    params.opts = opts;
                    new TraceInvokeResultHandler().exec(params);

                    System.out.println("\n==============================");
                    opts = new TraceResultOptions();
                    opts.displayError = opts.displayArgs = opts.displayReturnValue = opts.displayTime = false;
                    params.opts = opts;
                    new TraceInvokeResultHandler().exec(params);
                }
        );
    }
}
