package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.tools.asm.ProxyRegInfo;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static test.server.asm.AsmTestUtils.doCheck;

public class AsmProxyConstructorTest extends AbstractTest {
    private final int count = 3;
    private static final String errorMsg = "xxxx";

    @Test
    public void testNoArgs() throws Exception {
        call("()V", false, new Class[0]);
    }

    @Test
    public void testThrowError() throws Exception {
        call("(I)V", true, new Class[]{int.class}, 333);
    }

    @Test
    public void testWithArgs() throws Exception {
        call("(ILjava/lang/String;)V", false, new Class[]{int.class, String.class}, 111, "xxxx");
    }

    private void call(String desc, boolean error, Class[] argTypes, Object... args) throws Exception {
        List<String> logList = new ArrayList<>();
        Class<?> newAClass = AsmTestUtils.prepareClassConstructor(count, logList, A.class, desc);
        try {
            ReflectionUtils.newInstance(newAClass, argTypes, args);
            if (error)
                fail();
        } catch (Exception e) {
            Throwable t = Utils.getMeaningfulCause(e);
            t.printStackTrace();
            assertTrue(t instanceof RuntimeException);
            assertEquals(errorMsg, t.getMessage());
        }
        doCheck(count, logList, error);
    }

    private Class<?> newClass(String desc) throws Exception {
        final int count = 3;
        List<String> logList = new ArrayList<>();

        Constructor constructor = ReflectionUtils.findConstructor(A.class, desc);
        ProxyRegInfo regInfo = new ProxyRegInfo(constructor);
        return AsmTestUtils.prepareClass(count, logList, regInfo);
    }

    private static class A {
        A() {
            System.out.println("No args");
        }

        A(int a) {
            throw new RuntimeException(errorMsg);
        }

        A(int a, String b, Long c) {
            System.out.println("a: " + a + ", b: " + b + ", c: " + c);
        }

        A(int a, String b) {
            this(a, b, 333L);
        }
    }
}
