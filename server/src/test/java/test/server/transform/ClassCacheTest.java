package test.server.transform;

import agent.base.utils.IOUtils;
import agent.server.transform.cache.ClassCache;
import agent.server.transform.cache.IncludeClassFilter;
import org.junit.Test;
import test.server.AbstractTest;
import test.server.TestClassLoader;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        assertTrue(A.class.isAssignableFrom(newA2Class));

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

    @Test
    public void testClasses() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassCache classCache = newClassCache(loader);

        String prefix = getClass().getName() + "\\$";
        checkFindClasses(
                classCache,
                loader,
                prefix + "[^$]*",
                true,
                Arrays.asList(Intf.class, SubIntf.class, A.class, A2.class, A3.class)
        );
        checkFindClasses(
                classCache,
                loader,
                prefix + "[^$]*",
                false,
                Arrays.asList(A.class, A2.class, A3.class)
        );

        checkFindClasses(
                classCache,
                loader,
                prefix + "A.*",
                true,
                Arrays.asList(A.class, A2.class, A3.class)
        );
        checkFindClasses(
                classCache,
                loader,
                prefix + "A2",
                true,
                Collections.singleton(A2.class)
        );
    }

    private void checkFindClasses(ClassCache classCache, ClassLoader loader, String regexp,
                                  boolean includeInterface, Collection<Class<?>> expectedClasses) {
        assertTrue(
                new HashSet<>(
                        classCache.findClasses(
                                loader,
                                Collections.singleton(regexp),
                                null,
                                includeInterface
                        )
                ).containsAll(expectedClasses)
        );
    }

    private List<Class<?>> newClasses(TestClassLoader loader, Class<?>... classes) throws Exception {
        List<Class<?>> newClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            String className = clazz.getName();
            byte[] bs = IOUtils.readBytes(
                    ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class")
            );
            newClasses.add(
                    loader.loadClass(className, bs)
            );
        }
        return newClasses;
    }

    private Class<?> newClass(Class<?> clazz, ClassLoader loader) throws Exception {
        TestClassLoader subLoader = new TestClassLoader(loader);
        return newClasses(subLoader, clazz).get(0);
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
