package test.transformer;

import agent.builtin.transformer.utils.DefaultValueConverter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DefaultValueConverterTest {
    private static final DefaultValueConverter converter = new DefaultValueConverter();

    @Test
    public void test() {
        byte[] bs = new byte[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test2() {
        short[] bs = new short[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test3() {
        int[] bs = new int[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test4() {
        long[] bs = new long[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test5() {
        float[] bs = new float[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test6() {
        double[] bs = new double[]{1, 2, 3};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test7() {
        char[] bs = new char[]{'a', 'b', 'c'};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test8() {
        boolean[] bs = new boolean[]{true, false, true};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test9() {
        Object[] bs = new Object[]{"sss", new Date(), null};
        check(
                Arrays.toString(bs),
                bs
        );
    }

    @Test
    public void test10() {
        Object[] bs = new Object[]{
                "sss",
                new Date(),
                null,
                new Class[]{null, null},
                new int[]{1, 2, 3},
                new String[]{"aa", "bb", "cc"}
        };
        check(
                Arrays.deepToString(bs),
                bs
        );
    }

    @Test
    public void test11() {
        Date[] bs = new Date[]{
                new Date()
        };
        check(
                Arrays.deepToString(bs),
                bs
        );
    }

    private void check(String expectedValue, Object v) {
        System.out.println(expectedValue);
        assertEquals(
                expectedValue,
                converter.valueToString(v)
        );
    }
}
