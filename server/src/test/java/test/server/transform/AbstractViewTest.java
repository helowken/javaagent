package test.server.transform;

import org.junit.BeforeClass;
import test.server.AbstractTest;
import test.server.TestProxy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractViewTest extends AbstractTest {
    protected static final String contextA = "testA";
    protected static final String contextB = "testB";

    @BeforeClass
    public static void prepare() {
        TestProxy proxy = new TestProxy();
        Map<Class<?>, String> map = new HashMap<>();
        map.put(A.class, ".*");
        map.put(A2.class, ".*");
        transformByAnnt(contextA, map, proxy);

        Map<Class<?>, String> map2 = new HashMap<>();
        map2.put(B.class, "<init>.*");
        map2.put(B2.class, ".*");
        transformByAnnt(contextB, map2, proxy);
    }

    protected static class A {
        void test() {
        }

        void test2(int a1, boolean a2) {
        }

        String test3(Date date) {
            return "xxx";
        }
    }

    protected static class A2 {

    }

    protected static class B {
        B() {
        }

        B(int a, short b) {
        }

        B(long a, Double b, Float c) {
        }
    }

    protected static class B2 {

    }
}
