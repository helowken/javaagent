package test.server.search;

import agent.server.transform.config.ClassFilterConfig;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.ClassSearcher;
import org.junit.BeforeClass;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassSearchTest extends AbstractTest {
    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static final ClassCache classCache = new ClassCache();

    @BeforeClass
    public static void beforeClassClassSearchTest() {
        new A();
        new A2();
        new B();
        new BBB();
    }

    @Test
    public void test() {
        check(
                null,
                null,
                Arrays.asList(
                        Intf.class, A.class, A2.class, B.class, BBB.class
                ),
                null
        );
    }

    @Test
    public void test2() {
        check(
                Arrays.asList(
                        "*A",
                        "*$B"
                ),
                null,
                Arrays.asList(
                        A.class, B.class
                ),
                Arrays.asList(
                        Intf.class, A2.class, BBB.class
                )
        );
    }

    @Test
    public void test3() {
        check(
                Arrays.asList(
                        "*A",
                        "*B"
                ),
                Collections.singleton("*B"),
                Collections.singleton(
                        A.class
                ),
                Arrays.asList(
                        Intf.class, A2.class, B.class, BBB.class
                )
        );
    }

    @Test
    public void test4() {
        check(
                Arrays.asList(
                        "*A",
                        "*B"
                ),
                Collections.singleton("*$B"),
                Arrays.asList(
                        A.class, BBB.class
                ),
                Arrays.asList(
                        Intf.class, A2.class, B.class
                )
        );
    }

    @Test
    public void test5() {
        check(
                null,
                Arrays.asList(
                        "*A*",
                        "*B"
                ),
                Collections.singleton(
                        Intf.class
                ),
                Arrays.asList(
                        A2.class, B.class, A.class, BBB.class
                )
        );
    }

    @Test
    public void test6() {
        check(
                Arrays.asList(
                        A.class.getName(),
                        BBB.class.getName()
                ),
                null,
                Arrays.asList(
                        A.class, BBB.class
                ),
                Arrays.asList(
                        A2.class, B.class, Intf.class
                )
        );
    }

    @Test
    public void test7() {
        check(
                Arrays.asList(
                        A.class.getName(),
                        BBB.class.getName()
                ),
                Collections.singleton(
                        "*A"
                ),
                Collections.singleton(
                        BBB.class
                ),
                Arrays.asList(
                        A2.class, B.class, Intf.class, A.class
                )
        );
    }

    private void check(Collection<String> includes, Collection<String> excludes, Collection<Class<?>> expectedIncludes, Collection<Class<?>> expectedExcludes) {
        ClassFilterConfig config = new ClassFilterConfig();
        if (includes != null)
            config.setIncludes(
                    new HashSet<>(includes)
            );
        if (excludes != null)
            config.setExcludes(
                    new HashSet<>(excludes)
            );
        Collection<Class<?>> classes = ClassSearcher.getInstance().search(loader, classCache, config);
        if (expectedIncludes != null)
            expectedIncludes.forEach(
                    clazz -> assertTrue(
                            "Include Fail: " + clazz,
                            classes.contains(clazz)
                    )
            );
        if (expectedExcludes != null) {
            expectedExcludes.forEach(
                    clazz -> assertFalse(
                            "Exclude Fail: " + clazz,
                            classes.contains(clazz)
                    )
            );
        }
    }

    public interface Intf {
    }

    public static class A implements Intf {
    }

    public static class A2 {
    }

    public static class B {
    }

    public static class BBB {
    }
}
