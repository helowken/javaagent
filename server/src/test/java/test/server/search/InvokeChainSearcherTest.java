package test.server.search;

import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.InvokeChainSearcher;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class InvokeChainSearcherTest extends AbstractTest {
    private static final boolean debugEnabled = true;
    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static final ClassCache classCache = new ClassCache();

    static {
        InvokeChainSearcher.debugEnabled = debugEnabled;
    }

    @Test
    public void test() throws Exception {
        doTest(
                getMethod(Invoker.class, "test"),
                getMethod(A.class, "test"),
                getMethod(Intf.class, "xxx"),
                getMethod(A2.class, "test2"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2"),
                getMethod(A4.class, "test2"),
                getMethod(B.class, "b1"),
                getMethod(C.class, "c1"),
                getMethod(C.class, "c2"),
                getMethod(D.class, "d1"),
                getMethod(D.class, "d2"),
                getConstructor(B.class),
                getConstructor(C.class),
                getConstructor(D.class)
        );
    }

    @Test
    public void test2() throws Exception {
        doTest(
                getMethod(Invoker.class, "test2"),
                getMethod(A.class, "test"),
                getMethod(Intf.class, "xxx"),
                getMethod(A2.class, "test2"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2"),
                getMethod(A4.class, "test"),
                getMethod(A4.class, "test2"),
                getMethod(B.class, "b1"),
                getMethod(C.class, "c1"),
                getMethod(C.class, "c2"),
                getMethod(D.class, "d1"),
                getMethod(D.class, "d2"),
                getConstructor(B.class),
                getConstructor(C.class),
                getConstructor(D.class)
        );
    }

    @Test
    public void test3() throws Exception {
        doTest(
                getMethod(Invoker.class, "test3"),
                getMethod(A2.class, "test2"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2"),
                getMethod(B.class, "b1"),
                getMethod(C.class, "c1"),
                getMethod(C.class, "c2"),
                getMethod(D.class, "d1"),
                getMethod(D.class, "d2"),
                getConstructor(B.class),
                getConstructor(C.class),
                getConstructor(D.class)
        );
    }

    @Test
    public void test4() throws Exception {
        doTest(
                getMethod(Invoker.class, "test4"),
                getMethod(A2.class, "test2"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2"),
                getMethod(A4.class, "test2"),
                getMethod(B.class, "b1"),
                getMethod(C.class, "c1"),
                getMethod(C.class, "c2"),
                getMethod(D.class, "d1"),
                getMethod(D.class, "d2"),
                getConstructor(B.class),
                getConstructor(C.class),
                getConstructor(D.class)
        );
    }

    @Test
    public void test5() throws Exception {
        doTest(
                getMethod(Invoker.class, "test5"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2")
        );
    }

    @Test
    public void test6() throws Exception {
        doTest(
                getMethod(Invoker.class, "test6"),
                getMethod(A4.class, "test2")
        );
    }

    @Test
    public void test7() throws Exception {
        doTest(
                getMethod(Invoker.class, "test7"),
                getMethod(A.class, "test"),
                getMethod(Intf.class, "xxx"),
                getMethod(A2.class, "test2"),
                getMethod(A3.class, "test"),
                getMethod(A3.class, "test2"),
                getMethod(A4.class, "test"),
                getMethod(A4.class, "test2"),
                getMethod(B.class, "b1"),
                getMethod(C.class, "c1"),
                getMethod(C.class, "c2"),
                getMethod(D.class, "d1"),
                getMethod(D.class, "d2"),
                getConstructor(B.class),
                getConstructor(C.class),
                getConstructor(D.class)
        );
    }

    @Test
    public void test8() throws Exception {
        doTest(
                getMethod(Invoker2.class, "test"),
                getMethod(H.class, "test"),
                getMethod(H3.class, "test")
        );
    }

    @Test
    public void test9() throws Exception {
        doTest(
                getMethod(Invoker2.class, "test2"),
                getMethod(H.class, "test2"),
                getMethod(Intf2.class, "yyy"),
                getMethod(H3.class, "yyy")
        );
    }

    private void doTest(Object... expectation) {
        assertNotNull(expectation);
        assertTrue(expectation.length > 0);
        Collection<DestInvoke> rsList = InvokeChainSearcher.search(
                loader,
                classCache,
                this::getClassData,
                Collections.singletonList(
                        new MethodInvoke(
                                (Method) expectation[0]
                        )
                ),
                null
        );
        if (debugEnabled) {
            System.out.println("=============================");
            rsList.forEach(System.out::println);
        }
        assertEquals(
                new TreeSet<>(
                        Stream.of(expectation)
                                .map(Object::toString)
                                .collect(Collectors.toSet())
                ),
                new TreeSet<>(
                        rsList.stream()
                                .map(DestInvoke::getInvokeEntity)
                                .map(Object::toString)
                                .collect(Collectors.toSet())
                )
        );
    }

    static class Invoker {
        A2 a1 = new A3();
        A a2 = new A4();
        A3 a3 = new A3();
        A4 a4 = new A4();
        Intf a5 = new A4();

        void test() {
            a1.test();
        }

        void test2() {
            a2.test();
        }

        void test3() {
            a1.test2();
        }

        void test4() {
            a2.test2();
        }

        void test5() {
            a3.test2();
        }

        void test6() {
            a4.test2();
        }

        void test7() {
            a5.test();
        }
    }

    interface Intf {
        void test();

        default void xxx() {
        }
    }

    static abstract class A implements Intf {
        @Override
        public void test() {
            xxx();
            test2();
        }

        public abstract void test2();
    }

    static class A2 extends A implements Intf {
        @Override
        public void test2() {
            new B().b1();
        }
    }

    static class A3 extends A2 {
        @Override
        public void test() {
        }

        @Override
        public void test2() {
            test();
        }
    }

    static class A4 extends A {
        @Override
        public void test() {

        }

        @Override
        public void test2() {

        }
    }

    static class B {
        void b1() {
            C c = new C();
            c.c1();
            c.c2();
        }
    }

    static class C {
        void c1() {
        }

        void c2() {
            new D().d1();
        }
    }

    static class D {
        void d1() {
            d2(3);
        }

        void d2(int v) {
            if (v == 0)
                return;
            d2(v - 1);
        }
    }

    static class Invoker2 {
        H2 h = new H3();

        void test() {
            h.test();
        }

        void test2() {
            h.test2();
        }
    }

    interface Intf2 {
        default void yyy() {
        }
    }

    static class H implements Intf2 {
        void test() {
        }

        void test2() {
            yyy();
        }
    }

    static class H2 extends H {

    }

    static class H3 extends H2 {
        @Override
        void test() {

        }

        @Override
        public void yyy() {

        }
    }
}
