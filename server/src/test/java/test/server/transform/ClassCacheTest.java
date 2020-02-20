package test.server.transform;

import agent.base.utils.IOUtils;
import agent.server.transform.cache.ClassCache;
import agent.server.transform.cache.IncludeClassFilter;
import org.junit.Test;
import test.server.AbstractTest;
import test.server.TestClassLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class ClassCacheTest extends AbstractTest {
    @Test
    public void testSubClassesAndSubTypes() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassCache classCache = newClassCache(loader);
        new A3();
        assertEquals(
                Collections.singletonList(A2.class),
                classCache.getSubClasses(loader, A.class, true)
        );
        assertEquals(
                Collections.singletonList(A3.class),
                classCache.getSubClasses(loader, A2.class, true)
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, A3.class)
                ),
                new HashSet<>(
                        classCache.getSubTypes(loader, A.class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Arrays.asList(SubIntf.class, A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        classCache.getSubTypes(loader, Intf.class, true)
                )
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        classCache.getSubTypes(loader, Intf.class, false)
                )
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        classCache.getSubTypes(loader, SubIntf.class, true)
                )
        );
        assertEquals(
                Collections.singleton(A.class),
                new HashSet<>(
                        classCache.getSubClasses(loader, SubIntf.class, true)
                )
        );

        assertEquals(
                Collections.singleton(SubIntf.class),
                new HashSet<>(
                        classCache.getSubClasses(loader, Intf.class, true)
                )
        );
        assertEquals(
                Collections.emptyList(),
                classCache.getSubClasses(loader, Intf.class, false)
        );
    }

    @Test
    public void test() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassCache classCache = newClassCache(loader);
        new A3();

        Class<?> newA2Class = newClass(A2.class, loader);
        Class<?> newA3Class = newClass(A3.class, newA2Class.getClassLoader());

        classCache.getSubClasses(loader, A.class, true).forEach(
                clazz -> System.out.println(clazz + ", loader: " + clazz.getClassLoader())
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, newA2Class)
                ),
                new HashSet<>(
                        classCache.getSubClasses(loader, A.class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Collections.singleton(newA3Class)
                ),
                new HashSet<>(
                        classCache.getSubClasses(loader, newA2Class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, newA2Class, A3.class, newA3Class)
                ),
                new HashSet<>(
                        classCache.getSubTypes(loader, A.class, true)
                )
        );
    }

    private Class<?> newClass(Class<?> clazz, ClassLoader loader) throws Exception {
        String className = clazz.getName();
        byte[] bs = IOUtils.readBytes(
                ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class")
        );
        TestClassLoader subLoader = new TestClassLoader(loader);
        return subLoader.loadClass(className, bs);
    }

    private ClassCache newClassCache(ClassLoader loader) {
        return new ClassCache(
                Collections.singletonMap(
                        loader,
                        new IncludeClassFilter(
                                Collections.singletonList("test\\..*")
                        )
                )
        );
    }

    public interface Intf {
    }

    public interface SubIntf extends Intf {
    }

    public static class A implements SubIntf {
    }

    public static class A2 extends A {
    }

    public static class A3 extends A2 {
    }
}
