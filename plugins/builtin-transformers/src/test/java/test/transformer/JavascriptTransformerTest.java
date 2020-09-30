package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.impl.JavascriptTransformer;
import agent.server.transform.impl.ScriptEngineMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JavascriptTransformerTest extends AbstractTest {
    private static final Map<Class<?>, byte[]> classToData = new HashMap<>();

    @Test
    public void test() throws Exception {
        new JsService();
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(JsService.class, "test");
        doTest(JsService.class, "test", classToMethodFilter, InvokeChainConfig.matchAll());
    }

    void doTest(Class<?> targetClass, String targetMethodName, Map<Class<?>, String> classToMethodFilter,
                InvokeChainConfig invokeChainConfig) throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    final String instanceKey = newTransformerKey();
                    String script = IOUtils.readToString(
                            getClass().getClassLoader().getResourceAsStream("test.js")
                    );
                    JavascriptTransformer transformer = new JavascriptTransformer();
                    transformer.setInstanceKey(instanceKey);
                    config.put("script", script);
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

                }
        );
    }

    public static class JsService {
        public void test() {
            test222("aaa");
            new File("b.out");
        }

        private int test222(String s) {
            System.out.println(s);
            try {
                throw new RuntimeException("xxxx");
            } catch (Exception e) {
            }
            return 555;
        }
    }
}