package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.ByCallChainCostTimeResultHandler;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.utils.JSONUtils;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.impl.ViewMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static agent.builtin.tools.CostTimeUtils.DEFAULT_RATES;

public class TraceInvokeTransformerTest2 extends AbstractTest {

    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> runWithFile(
                        (outputPath2, config2) -> {
                            TraceInvokeTransformer traceTransformer = new TraceInvokeTransformer();
                            traceTransformer.setInstanceKey(
                                    newTransformerKey()
                            );
                            CostTimeStatisticsTransformer costTimeTransformer = new CostTimeStatisticsTransformer();
                            costTimeTransformer.setInstanceKey(
                                    newTransformerKey()
                            );

                            Map<ConfigTransformer, Map<String, Object>> transformerToConfig = new HashMap<>();
                            transformerToConfig.put(
                                    traceTransformer,
                                    config
                            );
                            transformerToConfig.put(
                                    costTimeTransformer,
                                    config2
                            );
                            String context = "test";
                            Map<Class<?>, String> classToMethodFilter = new HashMap<>();
                            classToMethodFilter.put(A.class, "test*");
                            classToMethodFilter.put(B.class, "*oad");

                            doTransform(transformerToConfig, context, classToMethodFilter, null);

                            Map<Class<?>, byte[]> classToData = getClassToData(
                                    new ArrayList<>(
                                            transformerToConfig.keySet()
                                    )
                            );

                            Object a = newInstance(classToData, A.class);
                            ReflectionUtils.invoke("test", a);
                            ReflectionUtils.invoke("test2", a);

                            System.out.println("=================");
                            Object b = newInstance(classToData, B.class);
                            ReflectionUtils.invoke("load", new Class[]{String.class}, b, "xxx");
                            ReflectionUtils.invoke("load", new Class[]{int.class}, b, 33);
                            ReflectionUtils.invoke("recursiveLoad", new Class[]{long.class}, b, (long) 4);

                            flushAndWaitMetadata(outputPath);
                            flushAndWaitMetadata(outputPath2);

                            TraceInvokeResultHandler.getInstance().printResult(outputPath);
                            System.out.println("=================");
                            new ByCallChainCostTimeResultHandler().printResult(outputPath2, false, DEFAULT_RATES);
                            System.out.println("=================");

                            System.out.println(
                                    JSONUtils.writeAsString(
                                            ViewMgr.create(ViewMgr.VIEW_PROXY, null, null, null, null),
                                            true
                                    )
                            );
                        }
                )
        );
    }

    static class A {
        public void test() {
            System.out.println("test.");
        }

        public static void test2() {
            System.out.println("test2.");
        }
    }

    static class B extends A {
        public static void load(String a) {
            System.out.println("load(String): " + a);
            load(33);
        }

        static void load(int n) {
            System.out.println("load(int): " + n);
        }

        static void recursiveLoad(long m) {
            if (m == 0) {
                System.out.println("recursiveLoad end.");
                return;
            }
            System.out.println("m=" + m);
            recursiveLoad(m - 1);
        }
    }
}
