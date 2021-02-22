package test.jvmti;

import agent.base.utils.FileUtils;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class JvmtiUtilsTest {
    private static final JvmtiUtils jvmtiUtils = JvmtiUtils.getInstance();

    static {
        jvmtiUtils.load(FileUtils.getUserDir() + "/src/main/native/libagent_jvmti_JvmtiUtils.so");
    }

    @Test
    public void testFindObjectByClass() throws Exception {
        new A();
        new A();
        new A();
        assertNotNull(jvmtiUtils.findObjectByClass(A.class));
        assertEquals(2, jvmtiUtils.findObjectsByClass(A.class, 2).size());
        assertEquals(3, jvmtiUtils.findObjectsByClass(A.class, 3).size());
        assertEquals(3, jvmtiUtils.findObjectsByClass(A.class, 4).size());
        assertNull(jvmtiUtils.findObjectByClass(B.class));

        assertNotNull(jvmtiUtils.findObjectByClassName(A.class.getName()));
        assertEquals(2, jvmtiUtils.findObjectsByClassName(A.class.getName(), 2).size());
        assertEquals(3, jvmtiUtils.findObjectsByClassName(A.class.getName(), 3).size());
        assertEquals(3, jvmtiUtils.findObjectsByClassName(A.class.getName(), 4).size());
        assertNull(jvmtiUtils.findObjectByClassName(B.class.getName()));


        new B();
        new SubB();
        new SubSubB();
        List<Class<?>> loadedClasses = jvmtiUtils.findLoadedClassList();
        ReflectionUtils.findSubTypes(T.class, loadedClasses).forEach(System.out::println);
        System.out.println("=================");
        ReflectionUtils.findSubClasses(T.class, loadedClasses).forEach(System.out::println);
        System.out.println("=================");
        ReflectionUtils.findSubTypes(B.class, loadedClasses).forEach(System.out::println);
        System.out.println("=================");
        ReflectionUtils.findSubClasses(B.class, loadedClasses).forEach(System.out::println);
        System.out.println("=================");
        ReflectionUtils.findSubTypes(SubB.class, loadedClasses).forEach(System.out::println);
        System.out.println("=================");
        ReflectionUtils.findSubClasses(SubB.class, loadedClasses).forEach(System.out::println);
    }

    private interface T {
    }

    private interface SubT extends T {
    }

    private static class A implements T {
    }

    private static class B implements T {
    }

    private static class SubB extends B {
    }

    private static class SubSubB extends SubB implements SubT {
    }
}
