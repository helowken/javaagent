package test.server.search;

import agent.common.config.ClassFilterConfig;
import agent.common.config.MethodFilterConfig;
import agent.common.config.TargetConfig;
import agent.invoke.DestInvoke;
import agent.server.transform.search.InvokeSearcher;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class InvokeSearcherTest {
    @Test
    public void test() {
        check(Intf.class, 1, 0);
        check(Base.class, 2, 1);
        check(Impl.class, 3, 1);
        check(PrivateFieldClass.class, 1, 0);
    }

    private void check(Class<?> clazz, int methodsCount, int meaningfulCount) {
        Collection<DestInvoke> invokes = search(clazz);
        assertEquals(methodsCount, clazz.getDeclaredMethods().length);
        assertEquals(meaningfulCount, invokes.size());
    }

    private Collection<DestInvoke> search(Class<?> clazz) {
        TargetConfig cc = new TargetConfig();
        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setIncludes(
                Collections.singleton(Impl.class.getName())
        );
        cc.setClassFilter(classFilterConfig);
        cc.setMethodFilter(new MethodFilterConfig());
        return InvokeSearcher.getInstance().search(
                clazz,
                cc.getMethodFilter(),
                cc.getConstructorFilter()
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
