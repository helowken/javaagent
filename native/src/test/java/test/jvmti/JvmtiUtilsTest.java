package test.jvmti;

import agent.jvmti.JvmtiUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JvmtiUtilsTest {
    private static final JvmtiUtils jvmtiUtils = JvmtiUtils.getInstance();
    static {
        jvmtiUtils.load("/home/helowken/test_jni/jni_jvmti/libagent_jvmti_JvmtiUtils.so");
    }

    @Test
    public void testFindObjectByClass() {
        new A();
        assertNotNull(jvmtiUtils.findObjectByClass(A.class));
        assertNull(jvmtiUtils.findObjectByClass(B.class));

        assertNotNull(jvmtiUtils.findObjectByClassName(A.class.getName()));
        assertNull(jvmtiUtils.findObjectByClassName(B.class.getName()));
    }

    private static class A {
    }

    private static class B {
    }
}
