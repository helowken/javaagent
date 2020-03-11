package test.server.transform;

import agent.server.transform.impl.DestInvokeIdRegistry;
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
        DestInvokeIdRegistry.getInstance().reset();
        TestProxy proxy = new TestProxy();
        Map<Class<?>, String> methodFilterMap = new HashMap<>();
        methodFilterMap.put(A.class, "*");
        Map<Class<?>, String> constructorFilterMap = new HashMap<>();
        constructorFilterMap.put(A.class, "*");
        constructorFilterMap.put(A2.class, "*");
        transformByAnnt(contextA, methodFilterMap, constructorFilterMap, proxy);

        constructorFilterMap = new HashMap<>();
        constructorFilterMap.put(B2.class, "*");
        constructorFilterMap.put(B.class, "<init>*");
        transformByAnnt(contextB, null, constructorFilterMap, proxy);
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
