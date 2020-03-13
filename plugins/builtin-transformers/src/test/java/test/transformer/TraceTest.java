package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.search.InvokeChainSearcher;
import org.junit.Test;
import test.server.AbstractTest;
import test.server.TestClassLoader;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TraceTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        InvokeChainSearcher.debugEnabled = true;
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(TestFilter3.class, "doFilter");

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        ConstructorFilterConfig constructorFilterConfig = new ConstructorFilterConfig();
        constructorFilterConfig.setIncludes(
                Collections.singleton("*")
        );
        invokeChainConfig.setConstructorFilter(constructorFilterConfig);
        doTest(classToMethodFilter, invokeChainConfig);
    }

    private void doTest(Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    TraceInvokeTransformer transformer = new TraceInvokeTransformer();
                    String context = "test";
                    doTransform(transformer, context, config, classToMethodFilter, invokeChainConfig);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    loader.loadClass(
                            ParamObject.class.getName(),
                            classToData.get(ParamObject.class)
                    );
                    Object a = new TestClassLoader(loader).loadClass(
                            TestFilter3.class.getName(),
                            classToData.get(TestFilter3.class)
                    ).newInstance();
                    ReflectionUtils.invoke("doFilter", a);

                    flushAndWaitMetadata(outputPath);
                    System.out.println(IOUtils.readToString(outputPath));

                    TraceInvokeResultHandler.getInstance().printResult(outputPath);
                }
        );
    }

    public static class TestFilter3 {

        public void doFilter() {
            ParamObject po = new ParamObject();
            System.out.println(po.test((byte) 0, (short) 1, 2, 3L, 4.4f, 5, true, "aaa"));
            System.out.println(po.test2());
        }

    }

    public static class ParamObject extends HashMap{
        private int a;

        public ParamObject() {
            this(3);
        }

        public ParamObject(int a) {
//            super(10);
            super(
                    Collections.singletonMap(
                            new HashMap(
                                    getCapacity()
                            ).toString(),
                            "xx"
                    )
            );
            this.a = a;
        }

        private static int getCapacity() {
            return 10;
        }

        public Date test(byte v0, short v1, int v2, long v3, float v4, double v5, boolean v6, String v7) {
            return new Date();
        }

        public boolean test2() {
            return false;
        }
    }
}

