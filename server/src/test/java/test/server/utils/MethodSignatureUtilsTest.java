package test.server.utils;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.impl.utils.AgentClassPool;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static agent.base.utils.MethodSignatureUtils.getSignature;
import static org.junit.Assert.assertEquals;

public class MethodSignatureUtilsTest {
    @Test
    public void test() throws Exception {
        check("f");
        check("f2");
        check("f3");
        check("f4");
    }

    private void check(String name) throws Exception {
        String signature = AgentClassPool.getInstance().get(getClass().getName()).getDeclaredMethod(name).getSignature();
        System.out.println(signature);
        assertEquals(
                signature,
                getSignature(getMethod(name)));
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

}
