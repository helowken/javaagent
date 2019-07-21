package test.jvmti;

import agent.jvmti.JvmtiUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JvmtiUtilsTest {
    @Test
    public void testFindObjectByClass() {
        new A();
        assertNotNull(JvmtiUtils.getInstance().findObjectByClass(A.class));
        assertNull(JvmtiUtils.getInstance().findObjectByClass(B.class));
    }

    private static class A {
    }

    private static class B {
    }
}
