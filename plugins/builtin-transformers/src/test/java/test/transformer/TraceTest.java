package test.transformer;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.NoRouteToHostException;
import java.util.*;
import java.util.stream.Stream;

public class TraceTest extends AbstractTraceTest {

    @Test
    public void test() throws Exception {
        test(TestFilter3.class, "doFilter", true);
    }

    public static class TestFilter3 {

        public void doFilter() {
            try {
                new ErrorObject();
            } catch (Exception e) {
            }

            ParamObject po = new ParamObject();
            System.out.println(po.test((byte) 0, 'a', (short) 1, 2, 3L, 4.4f, 5, true, "aaa"));
            System.out.println(po.test2(
                    new int[]{9, 8, 7},
                    new Boolean[]{false, true}
            ));

            Base[] bs = new Impl[2];
            bs[0] = new Impl();
            bs[1] = new Impl();
            po.test3(bs);

            System.out.println(
                    po.test4(
                            () -> System.out.println(
                                    Format.parse("AAA")
                            )
                    )
            );

            po.test5(new NewMap(), "newKey");

            po.test7(6);
            po.test8();

            try {
                po.test6();
            } catch (Exception e) {
                throw new RuntimeException("BBB", e);
            }
        }
    }


    public static class ErrorObject {
        public ErrorObject() {
            try {
                try {
                    throw new RuntimeException("YYYYY");
                } catch (Exception e) {
                    System.out.println(2222);
                    throw e;
                }
            } catch (Exception e) {
                System.out.println(3333);
                throw new IllegalArgumentException("HHHH", e);
            }
        }
    }

    public static class ParamObject extends HashMap {
        private int a;

        public ParamObject() {
            this(3);
        }

        public ParamObject(int a) {
//            super(10);
            super(
                    Collections.singletonMap(
                            new HashMap(
                                    getCapacity()
                            ).toString(),
                            "xx"
                    )
            );
            this.a = a;
            List<Exception> errorList = Arrays.asList(
                    new FileNotFoundException("File XXXXX not found."),
                    new NoRouteToHostException(),
                    new IllegalArgumentException("Argument aaa is invalid."),
                    new Exception()
            );
            for (Exception error : errorList) {
                try {
                    throw error;
                } catch (FileNotFoundException | NoRouteToHostException | IllegalArgumentException e) {
                } catch (Exception e) {
                }
            }
        }

        private static int getCapacity() {
            return 10;
        }

        public Date test(byte v0, char c, short v1, int v2, long v3, float v4, double v5, boolean v6, String v7) {
            Base b = new Impl();
            b.doIt();
            return new Date();
        }

        public boolean test2(int[] as, Boolean[] bs) {
            System.out.println(
                    Arrays.toString(as) + '\n' + Arrays.toString(bs)
            );
            return false;
        }

        public void test3(Base[] bs) {
            Stream.of(bs).forEach(Base::doIt);
        }

        public String test4(Help help) {
            help.call();
            return null;
        }

        public void test5(Map<String, Object> map, String key) {
            System.out.println(key + ": " + map.get(key));
        }

        public void test6() {
            throw new RuntimeException("xxx");
        }

        public void test7(int v) {
            System.out.println("v: " + v);
            if (v == 0)
                return;
            test7(v - 1);
        }

        public void test8() {
            test81();
        }

        private void test81() {
            test811();
        }

        private void test811() {
            test8111();
        }

        private void test8111() {
        }
    }

    public static abstract class Base {
        public abstract void doIt();

        public String toString(Base v) {
            return "1\"1\"1-" + System.identityHashCode(v);
        }
    }

    public static class Impl extends Base {

        @Override
        public void doIt() {
            System.out.println(
                    this.toString()
            );
        }

        @Override
        public String toString() {
            return super.toString(this);
        }
    }

    public enum Format {

        AAA("AAA"),
        BBB("BBB");

        private String value;

        Format(String value) {
            this.value = value;
        }

        public static Format parse(String value) {
            if (value != null) {
                for (Format tokenFormat : values()) {
                    if (tokenFormat.value.equalsIgnoreCase(value)) {
                        return tokenFormat;
                    }
                }
            }
            throw new RuntimeException("xxx");
        }
    }

    public interface Help {
        void call();
    }

    public static class NewMap extends HashMap<String, Object> {
        @Override
        public Object get(Object key) {
            return "valueFromNewMap";
        }
    }
}

