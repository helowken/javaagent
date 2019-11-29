package test.server.method;

import agent.server.transform.MethodFinder;
import agent.server.transform.MethodFinder.MethodSearchResult;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.TargetClassConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodFinderTest {
    @Test
    public void test() {
        check(Intf.class, 1, 0);
        check(Base.class, 2, 1);
        check(Impl.class, 3, 1);
        check(PrivateFieldClass.class, 1, 0);
    }

    private void check(Class<?> clazz, int methodsCount, int meaningfulCount) {
        MethodSearchResult result = search(clazz);
        assertEquals(methodsCount, clazz.getDeclaredMethods().length);
        assertEquals(meaningfulCount, result.methods.size());
    }

    private MethodSearchResult search(Class<?> clazz) {
        ClassConfig cc = new ClassConfig();
        cc.setTargetClass(Impl.class.getName());
        cc.setMethodFilter(new MethodFilterConfig());
        TargetClassConfig tcc = new TargetClassConfig(clazz, cc);
        return MethodFinder.getInstance().find(tcc);
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
