package test;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RemoveFinalTest {
    @Test
    public void test() throws Exception {
        Field field = A.class.getDeclaredField("a");
        System.out.println("Before: " + Modifier.isFinal(field.getModifiers()));
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        System.out.println("After: " + Modifier.isFinal(field.getModifiers()));

        System.out.println("============================");
        Method method = A.class.getDeclaredMethod("test");
        System.out.println("Before: " + Modifier.isFinal(method.getModifiers()));
        modifiersField = Method.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(method, method.getModifiers() & ~Modifier.FINAL);
        System.out.println("After: " + Modifier.isFinal(method.getModifiers()));

        System.out.println("============================");
        Class<A> aClass = A.class;
        System.out.println(aClass.getModifiers());
    }

    private static final class A {
        final int a;

        private A(int a) {
            this.a = a;
        }

        final void test() {
        }
    }
}
