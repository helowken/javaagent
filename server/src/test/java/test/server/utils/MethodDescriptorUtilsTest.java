package test.server.utils;

import org.junit.Test;
import org.objectweb.asm.Type;
import test.server.AbstractTest;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.Function;

import static agent.base.utils.MethodDescriptorUtils.descToText;
import static agent.base.utils.MethodDescriptorUtils.getDescriptor;
import static org.junit.Assert.assertEquals;

public class MethodDescriptorUtilsTest extends AbstractTest {

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

    private void checkText(String name) throws Exception {
        Method method = getMethod(getClass(), name);
        String signature = Type.getMethodDescriptor(method);
        String text = descToText(name + signature);
        assertEquals(
                method.toString().replace("test.server.utils.MethodDescriptorUtilsTest.", "").replaceAll(",", ", "),
                "private " + text
        );
    }

    private void checkDesc(String name) throws Exception {
        String desc = Type.getMethodDescriptor(
                getMethod(getClass(), name)
        );
        System.out.println(desc);
        assertEquals(
                desc,
                getDescriptor(
                        getMethod(
                                getClass(),
                                name
                        )
                )
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
}
