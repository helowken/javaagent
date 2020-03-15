package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.search.InvokeChainSearcher;
import agent.server.transform.tools.asm.AsmUtils;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
                    // load all classes including lamda class
                    new TestFilter3().doFilter();

                    TraceInvokeTransformer transformer = new TraceInvokeTransformer();
                    String context = "test";
                    doTransform(transformer, context, config, classToMethodFilter, invokeChainConfig);

                    Map<Class<?>, byte[]> classToData = getClassToData(transformer);
                    Map<String, Class<?>> nameToClass = new HashMap<>();
                    classToData.forEach(
                            (clazz, data) -> Utils.wrapToRtError(
                                    () -> nameToClass.put(
                                            clazz.getName(),
                                            loader.loadClass(
                                                    clazz.getName(),
                                                    data
                                            )
                                    )
                            )
                    );
                    Object a = nameToClass.get(
                            TestFilter3.class.getName()
                    ).newInstance();
                    ReflectionUtils.invoke("doFilter", a);

                    flushAndWaitMetadata(outputPath);
                    System.out.println(IOUtils.readToString(outputPath));

                    TraceInvokeResultHandler.getInstance().printResult(outputPath);
                }
        );
//
//        System.out.println("================================");
//        AsmUtils.print(
//                getClassData(TestFilter3.class),
//                System.out
//        );
//        System.out.println("================================");
//        AsmUtils.print(
//                getClassData(ParamObject.class),
//                System.out
//        );
    }

    public static class TestFilter3 {

        public void doFilter() {
            ParamObject po = new ParamObject();
            System.out.println(po.test((byte) 0, (short) 1, 2, 3L, 4.4f, 5, true, "aaa"));
            System.out.println(po.test2());

            Base[] bs = new Impl[2];
            bs[0] = new Impl();
            bs[1] = new Impl();
            po.test3(bs);

            po.test4(
                    () -> System.out.println(
                            Format.parse("AAA")
                    )
            );
        }

    }

    public static class ParamObject extends HashMap {
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
            Base b = new Impl();
            b.doIt();
            return new Date();
        }

        public boolean test2() {
            return false;
        }

        public void test3(Base[] bs) {
            Stream.of(bs).forEach(Base::doIt);
        }

        public void test4(Help help) {
            help.call();
        }
    }

    public static abstract class Base {
        public abstract void doIt();
    }

    public static class Impl extends Base {

        @Override
        public void doIt() {
            System.out.println(111);
        }
    }

    public enum Format {

        AAA("AAA"),
        BBB("BBB");

        private String value;

        Format(String value) {
            this.value = value;
        }

        public static Format parse(String value) {
            if (value != null) {
                for (Format tokenFormat : values()) {
                    if (tokenFormat.value.equalsIgnoreCase(value)) {
                        return tokenFormat;
                    }
                }
            }
            throw new RuntimeException("xxx");
        }
    }

    public interface Help {
        void call();
    }
}

