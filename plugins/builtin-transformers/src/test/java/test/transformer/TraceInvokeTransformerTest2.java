package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.CostTimeCallChainResultHandler;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.CostTimeCallChainResultParamParser;
import agent.builtin.tools.result.parse.CostTimeResultParams;
import agent.builtin.tools.result.parse.TraceResultParamParser;
import agent.builtin.tools.result.parse.TraceResultParams;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.InfoQuery;
import agent.common.config.TargetConfig;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.impl.InfoMgr;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TraceInvokeTransformerTest2 extends AbstractTest {

    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> runWithFile(
                        (outputPath2, config2) -> {
                            TraceInvokeTransformer traceTransformer = new TraceInvokeTransformer();
                            traceTransformer.setTid(
                                    newTransformerKey()
                            );
                            CostTimeStatisticsTransformer costTimeTransformer = new CostTimeStatisticsTransformer();
                            costTimeTransformer.setTid(
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
                            Map<Class<?>, String> classToMethodFilter = new HashMap<>();
                            classToMethodFilter.put(A.class, "test*");
                            classToMethodFilter.put(B.class, "*oad");

                            doTransform(transformerToConfig, classToMethodFilter, null);

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

                            flushAndWaitMetadata(costTimeTransformer.getTid());
                            flushAndWaitMetadata(traceTransformer.getTid());

                            TraceResultParamParser parser = new TraceResultParamParser();
                            TraceInvokeResultHandler traceHandler = new TraceInvokeResultHandler();
                            TraceResultParams traceParams = parser.parse(
                                    new String[]{outputPath}
                            );
                            traceHandler.exec(traceParams);

                            System.out.println("\n=================");
                            traceParams = parser.parse(
                                    new String[]{"-f", "m=recursive*", outputPath}
                            );
                            traceHandler.exec(traceParams);

                            System.out.println("\n=================");
                            traceParams = parser.parse(
                                    new String[]{"-f", "m=recursive*; sl=3", outputPath}
                            );
                            traceHandler.exec(traceParams);

                            System.out.println("\n=================");
                            traceParams = parser.parse(
                                    new String[]{"-f", "m=load; sl=1", outputPath}
                            );
                            traceHandler.exec(traceParams);

                            System.out.println("\n=================");
                            CostTimeCallChainResultHandler costTimeHandler = new CostTimeCallChainResultHandler();
                            CostTimeCallChainResultParamParser timeParser = new CostTimeCallChainResultParamParser();
                            CostTimeResultParams costTimeParams = timeParser.parse(
                                    new String[]{outputPath2}
                            );
                            costTimeHandler.exec(costTimeParams);

                            System.out.println("\n=================");
                            costTimeParams = timeParser.parse(
                                    new String[]{"-f", "m=test", outputPath2}
                            );
                            costTimeHandler.exec(costTimeParams);

                            System.out.println("\n=================");
                            costTimeParams = timeParser.parse(
                                    new String[]{"-f", "cm=recursive*", outputPath2}
                            );
                            costTimeHandler.exec(costTimeParams);

                            System.out.println("\n=================");
                            costTimeParams = timeParser.parse(
                                    new String[]{"-f", "cm=recursive*; sl=3", outputPath2}
                            );
                            costTimeHandler.exec(costTimeParams);

                            System.out.println("\n=================");
                            InfoQuery infoQuery = new InfoQuery();
                            infoQuery.setLevel(InfoQuery.INFO_PROXY);
                            infoQuery.setTargetConfig(new TargetConfig());
                            System.out.println(
                                    new ObjectMapper()
                                            .writerWithDefaultPrettyPrinter()
                                            .writeValueAsString(
                                                    InfoMgr.create(infoQuery)
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
                System.out.println("recursiveLoad finish.");
                return;
            }
            System.out.println("m=" + m);
            recursiveLoad(m - 1);
        }
    }
}
