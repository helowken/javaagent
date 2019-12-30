package test.transformer;

import agent.base.utils.ReflectionUtils;
import agent.builtin.transformer.CostTimeMeasureTransformer;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CostTimeMeasureTransformerTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        CostTimeMeasureTransformer transformer = new CostTimeMeasureTransformer();
        String context = "test";
        Map<Class<?>, String> classToMethodFilter = new HashMap<>();
        classToMethodFilter.put(A.class, "test.*");
        classToMethodFilter.put(B.class, ".*[l|L]oad");
        doTransform(transformer, context, Collections.emptyMap(), classToMethodFilter);

        Map<Class<?>, byte[]> classToData = getClassToData(transformer);

        Object a = newInstance(classToData, A.class);
        ReflectionUtils.invoke("test", a);
        ReflectionUtils.invoke("test2", a);

        System.out.println("=================");
        Object b = newInstance(classToData, B.class);
        ReflectionUtils.invoke("load", new Class[]{String.class}, b, "xxx");
        ReflectionUtils.invoke("load", new Class[]{int.class}, b, 33);
        ReflectionUtils.invoke("recursiveLoad", new Class[]{long.class}, b, (long) 4);

        flushAndWaitMetadata();
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
