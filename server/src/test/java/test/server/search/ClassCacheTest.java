package test.server.search;

import agent.base.utils.IOUtils;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.filter.FilterUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import test.server.AbstractTest;
import test.server.TestClassLoader;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassCacheTest extends AbstractTest {

    @BeforeClass
    public static void beforeClassClassCacheTest() {
        new A3();
    }

    @Test
    public void testSubClassesAndSubTypes() {
        ClassCache classCache = new ClassCache();
        assertEquals(
                Collections.singletonList(A2.class),
                getSubClasses(classCache, A.class, true)
        );
        assertEquals(
                Collections.singletonList(A3.class),
                getSubClasses(classCache, A2.class, true)
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, A3.class)
                ),
                new HashSet<>(
                        getSubTypes(classCache, A.class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Arrays.asList(SubIntf.class, A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        getSubTypes(classCache, Intf.class, true)
                )
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        getSubTypes(classCache, Intf.class, false)
                )
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A.class, A2.class, A3.class)
                ),
                new HashSet<>(
                        getSubTypes(classCache, SubIntf.class, true)
                )
        );
        assertEquals(
                Collections.singleton(A.class),
                new HashSet<>(
                        getSubClasses(classCache, SubIntf.class, true)
                )
        );

        assertEquals(
                Collections.singleton(SubIntf.class),
                new HashSet<>(
                        getSubClasses(classCache, Intf.class, true)
                )
        );
        assertEquals(
                Collections.emptyList(),
                getSubClasses(classCache, Intf.class, false)
        );
    }

    @Test
    public void test() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassCache classCache = new ClassCache();

        Class<?> newA2Class = newClass(A2.class, loader);
        Class<?> newA3Class = newClass(A3.class, newA2Class.getClassLoader());

        assertTrue(A.class.isAssignableFrom(newA2Class));

        getSubClasses(classCache, A.class, true).forEach(
                clazz -> System.out.println(clazz + ", loader: " + clazz.getClassLoader())
        );
        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, newA2Class)
                ),
                new HashSet<>(
                        getSubClasses(classCache, A.class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Collections.singleton(newA3Class)
                ),
                new HashSet<>(
                        getSubClasses(classCache, newA2Class, true)
                )
        );

        assertEquals(
                new HashSet<>(
                        Arrays.asList(A2.class, newA2Class, A3.class, newA3Class)
                ),
                new HashSet<>(
                        getSubTypes(classCache, A.class, true)
                )
        );
    }

    @Test
    public void testClasses() {
        ClassCache classCache = new ClassCache();

        String prefix = getClass().getName() + "$";
        checkFindClasses(
                classCache,
                prefix + "*",
                true,
                Arrays.asList(Intf.class, SubIntf.class, A.class, A2.class, A3.class)
        );
        checkFindClasses(
                classCache,
                prefix + "*",
                false,
                Arrays.asList(A.class, A2.class, A3.class)
        );

        checkFindClasses(
                classCache,
                prefix + "A*",
                true,
                Arrays.asList(A.class, A2.class, A3.class)
        );

        FilterUtils.newClassFilter(
                Collections.singleton(prefix + "A2"),
                null,
                true
        ).accept(A2.class);
        checkFindClasses(
                classCache,
                prefix + "A2",
                true,
                Collections.singleton(A2.class)
        );
    }

    private Collection<Class<?>> getSubClasses(ClassCache classCache, Class<?> baseClass, boolean includeInterface) {
        return classCache.getSubClasses(
                baseClass,
                FilterUtils.newClassFilter(null, null, includeInterface)
        );
    }

    private Collection<Class<?>> getSubTypes(ClassCache classCache, Class<?> baseClass, boolean includeInterface) {
        return classCache.getSubTypes(
                baseClass,
                FilterUtils.newClassFilter(null, null, includeInterface)
        );
    }

    private void checkFindClasses(ClassCache classCache, String regexp,
                                  boolean includeInterface, Collection<Class<?>> expectedClasses) {
        Collection<Class<?>> classes = classCache.findClasses(
                FilterUtils.newClassFilter(
                        Collections.singleton(regexp),
                        null,
                        includeInterface
                )
        );
        expectedClasses.forEach(
                clazz -> assertTrue(
                        "Fail: " + clazz,
                        classes.contains(clazz)
                )
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
