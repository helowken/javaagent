package test;

import agent.base.utils.ReflectionUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeTest {
    public static void main(String[] args) throws Exception {
        Unsafe unsafe = ReflectionUtils.getStaticFieldValue(
                Unsafe.class,
                "theUnsafe"
        );
        A a = new A();
        a.b = new B();
//        System.out.println("=======111: " + a.a);
        Field field = ReflectionUtils.getField(A.class, "a");
        long addr = unsafe.objectFieldOffset(field);
//        Object value = unsafe.getObject(a, addr);
//        System.out.println(value);
        unsafe.putObject(a, addr, "bbb");
        System.out.println("=======222: " + a.getA());
        Object value = unsafe.getObject(a, addr);
        System.out.println(value);


        System.out.println(
                unsafe.getObject(
                        a,
                        unsafe.objectFieldOffset(
                                ReflectionUtils.getField(A.class, "b")
                        )
                )
        );

    }

    private static class A {
        private final String a = "aaa";
        private B b;

        String getA() {
            return a;
        }
    }

    private static class B {
        private int b = 333;
    }
}
