package test.server.utils;

import agent.base.utils.InvokeDescriptorUtils;
import org.junit.Test;
import org.objectweb.asm.Type;
import test.server.AbstractTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.Function;

import static agent.base.utils.InvokeDescriptorUtils.descToText;
import static agent.base.utils.InvokeDescriptorUtils.getDescriptor;
import static org.junit.Assert.assertEquals;

public class InvokeDescriptorUtilsTest extends AbstractTest {

    @Test
    public void testGetDesc() throws Exception {
        checkDesc("f");
        checkDesc("f2");
        checkDesc("f3");
        checkDesc("f4");
        checkDesc("f5");
    }

    @Test
    public void testDescToText() throws Exception {
        checkText("f");
        checkText("f2");
        checkText("f3");
        checkText("f4");
        checkText("f5");
    }

    @Test
    public void testConstructorDesc() throws Exception {
        checkConstructorDesc(A.class);
        checkConstructorDesc(B.class);
        checkConstructorDesc(C.class);
    }

    @Test
    public void testConstructorText() throws Exception {
        checkConstructorText(A.class);
        checkConstructorText(B.class);
        checkConstructorText(C.class);
    }

    private void checkConstructorDesc(Class<?> clazz) throws Exception {
        Constructor constructor = getConstructor(clazz);
        assertEquals(
                Type.getConstructorDescriptor(constructor),
                getDescriptor(constructor)
        );
    }

    private void checkConstructorText(Class<?> clazz) throws Exception {
        Constructor constructor = getConstructor(clazz);
        String desc = Type.getConstructorDescriptor(constructor);
        InvokeDescriptorUtils.TextConfig config = new InvokeDescriptorUtils.TextConfig();
        config.shortForPkgLang = false;
        config.withReturnType = false;
        assertEquals(
                constructor.toString().replaceAll(".*\\(", "(").replaceAll(",", ", "),
                descToText(desc, config).replaceAll(".* \\(", "(")
        );
    }

    private void checkText(String name) throws Exception {
        Method method = getMethod(getClass(), name);
        String desc = Type.getMethodDescriptor(method);
        InvokeDescriptorUtils.TextConfig config = new InvokeDescriptorUtils.TextConfig();
        config.shortForPkgLang = false;
        config.returnTypeAtTheEnd = false;
        String text = descToText(name + desc, config);
        assertEquals(
                method.toString().replace(getClass().getName() + ".", "").replaceAll(",", ", "),
                "private " + text
        );
    }

    private void checkDesc(String name) throws Exception {
        Method method = getMethod(getClass(), name);
        assertEquals(
                Type.getMethodDescriptor(method),
                getDescriptor(method)
        );
    }

    private long f(int n, String s, int[] arr) {
        return 0;
    }

    private void f2(Integer n, Date s, Boolean[] arr) {
    }

    private Iterable<Long> f3() {
        return null;
    }

    private Date f4(boolean a, byte b, char c, short d, int e, long f, float g, double h, Void i, Function[] funcs) {
        return null;
    }

    private String[][][][][] f5(boolean[][] a, short[][][] b, int[][][][] c) {
        return null;
    }

    static class A {
        A() {
        }
    }

    static class B {
        public B(String a1, int a2, Date a3) {
        }
    }

    static class C {
        public C(Long a1, float a2) {
        }
    }
}
