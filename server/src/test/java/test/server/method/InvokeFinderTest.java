package test.server.method;

import agent.server.transform.InvokeSearcher;
import agent.server.transform.InvokeSearcher.InvokeSearchResult;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.ClassFilterConfig;
import agent.server.transform.config.MethodFilterConfig;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class InvokeFinderTest {
    @Test
    public void test() {
//        check(Intf.class, 1, 0);
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
        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setClasses(
                Collections.singleton(Impl.class.getName())
        );
        cc.setClassFilter(classFilterConfig);
        cc.setMethodFilter(new MethodFilterConfig());
        return InvokeSearcher.getInstance().find(
                clazz,
                Collections.singletonList(
                        cc.getMethodFilter()
                )
        );
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
