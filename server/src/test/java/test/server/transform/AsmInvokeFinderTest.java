package test.server.transform;

import agent.server.transform.cache.ClassCache;
import agent.server.transform.cache.IncludeClassFilter;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.tools.asm.AsmInvokeFinder;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AsmInvokeFinderTest extends AbstractTest {
    private static final boolean debugEnabled = false;

    @Test
    public void test() throws Exception {
        AsmInvokeFinder.debugEnabled = debugEnabled;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassCache classCache = new ClassCache(
                Collections.singletonMap(
                        loader,
                        new IncludeClassFilter(
                                Collections.singletonList("test\\..*")
                        )
                )
        );
        doTest(
                loader,
                classCache,
                "test",
                Arrays.asList(
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
                )
        );

        doTest(
                loader,
                classCache,
                "test2",
                Arrays.asList(
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
                )
        );

        doTest(
                loader,
                classCache,
                "test3",
                Arrays.asList(
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
                )
        );

        doTest(
                loader,
                classCache,
                "test4",
                Arrays.asList(
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
                )
        );

        doTest(
                loader,
                classCache,
                "test5",
                Arrays.asList(
                        getMethod(Invoker.class, "test5"),
                        getMethod(A3.class, "test"),
                        getMethod(A3.class, "test2")
                )
        );

        doTest(
                loader,
                classCache,
                "test6",
                Arrays.asList(
                        getMethod(Invoker.class, "test6"),
                        getMethod(A4.class, "test2")
                )
        );

        doTest(
                loader,
                classCache,
                "test7",
                Arrays.asList(
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
                )
        );
    }

    private void doTest(ClassLoader loader, ClassCache classCache, String methodName, Collection<Object> expectation) throws Exception {
        Collection<DestInvoke> rsList = AsmInvokeFinder.find(
                Collections.singletonMap(
                        loader,
                        Collections.singletonList(
                                new MethodInvoke(
                                        Invoker.class.getDeclaredMethod(methodName)
                                )
                        )
                ),
                loader,
                classCache,
                this::getClassData
        );
        if (debugEnabled) {
            System.out.println("=============================");
            rsList.forEach(System.out::println);
        }
        assertEquals(
                new TreeSet<>(
                        expectation.stream()
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

    static class A2 extends A {
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
}
