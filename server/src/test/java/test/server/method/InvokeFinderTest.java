package test.server.method;

import agent.server.transform.InvokeFinder;
import agent.server.transform.InvokeFinder.InvokeSearchResult;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.InvokeFilterConfig;
import agent.server.transform.impl.TargetClassConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InvokeFinderTest {
    @Test
    public void test() {
        check(Intf.class, 1, 0);
        check(Base.class, 2, 1);
        check(Impl.class, 3, 1);
        check(PrivateFieldClass.class, 1, 0);
    }

    private void check(Class<?> clazz, int methodsCount, int meaningfulCount) {
        InvokeSearchResult result = search(clazz);
        assertEquals(methodsCount, clazz.getDeclaredMethods().length);
        assertEquals(meaningfulCount, result.invokes.size());
    }

    private InvokeSearchResult search(Class<?> clazz) {
        ClassConfig cc = new ClassConfig();
        cc.setTargetClass(Impl.class.getName());
        cc.setInvokeFilter(new InvokeFilterConfig());
        TargetClassConfig tcc = new TargetClassConfig(clazz, cc);
        return InvokeFinder.getInstance().find(tcc);
    }

    interface Intf<T> {
        void test(T a);
    }

    static class Base<T extends Base> implements Intf<T> {

        @Override
        public void test(T a) {
        }
    }

    static class Impl extends Base<Impl> {
        @Override
        public void test(Impl a) {
        }
    }

    private class PrivateFieldClass {
        private int a = 3;
    }

    private class PrivateClass {
        private void privateTest() {
            System.out.println(new PrivateFieldClass().a);
        }
    }
}
