package test.server.asm;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.tools.asm.ProxyRegInfo;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AsmProxyConstructorTest {
    @Test
    public void test() throws Exception {
        Class<?> newAClass = newClass("()V");
        ReflectionUtils.newInstance(newAClass);
        System.out.println("========================");
    }

    @Test
    public void test2() throws Exception {
        Class<?> newAClass = newClass("(ILjava/lang/String;Ljava/lang/Long;)V");
        ReflectionUtils.newInstance(
                newAClass,
                new Object[]{
                        int.class,
                        String.class,
                        Long.class
                },
                111,
                "xxxx",
                555L
        );
        System.out.println("========================");
    }

    @Test
    public void test3() throws Exception {
        Class<?> newAClass = newClass("(ILjava/lang/String;)V");
        ReflectionUtils.newInstance(
                newAClass,
                new Object[]{
                        int.class,
                        String.class
                },
                111,
                "xxxx"
        );
        System.out.println("========================");
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

        A(int a, String b, Long c) {
            System.out.println("a: " + a + ", b: " + b + ", c: " + c);
        }

        A(int a, String b) {
            this(a, b, 333L);
        }
    }
}
