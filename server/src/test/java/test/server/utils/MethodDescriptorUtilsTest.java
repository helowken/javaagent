package test.server.utils;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.cp.AgentClassPool;
import org.junit.BeforeClass;
import org.junit.Test;
import test.server.AbstractServerTest;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static agent.base.utils.MethodDescriptorUtils.descToText;
import static agent.base.utils.MethodDescriptorUtils.getDescriptor;
import static org.junit.Assert.assertEquals;

public class MethodDescriptorUtilsTest extends AbstractServerTest {
    private static AgentClassPool cp;

    @BeforeClass
    public static void beforeClassMethodSignatureUtilsTest() {
        cp = new AgentClassPool(defaultContext);
        classFinder.setContextLoader(defaultContext);
    }

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
        String signature = cp.get(getClass().getName()).getDeclaredMethod(name).getSignature();
        String text = descToText(name + signature);
        assertEquals(
                getMethod(name).toString().replace("test.server.utils.MethodDescriptorUtilsTest.", "").replaceAll(",", ", "),
                "private " + text
        );
    }

    private void checkDesc(String name) throws Exception {
        String signature = cp.get(getClass().getName()).getDeclaredMethod(name).getSignature();
        System.out.println(signature);
        assertEquals(
                signature,
                getDescriptor(getMethod(name)));
    }

    private Method getMethod(String name) throws Exception {
        return Optional.ofNullable(
                ReflectionUtils.findFirstMethod(getClass(), name)
        ).orElseThrow(
                () -> new RuntimeException("No method found by name: " + name)
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
