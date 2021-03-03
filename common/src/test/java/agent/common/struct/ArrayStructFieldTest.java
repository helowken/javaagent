package agent.common.struct;

import agent.base.struct.impl.Struct;
import org.junit.Test;
import utils.TestUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class ArrayStructFieldTest {
    @Test
    public void test() {
//        if (true) {
//            Object[][][] bb = new Object[2][][];
//            bb[0] = new Byte[3][];
//            bb[1] = new String[2][];
//            return;
//        }
        doTest(null);
        doTest(new boolean[0]);
        doTest(new Boolean[0]);

        doTest(
                new boolean[]{
                        true, false, false, true
                }
        );
        doTest(
                new Boolean[]{
                        true, false, false, true
                }
        );
//        doTest(
//                new boolean[][] {
//                        new boolean[]{
//                                true, false, false, true
//                        },
//                        new boolean[]{
//                                true, false
//                        }
//                }
//        );

        doTest(
                new byte[]{
                        0, 1, 2, 3, 5, 7, 9, Byte.MAX_VALUE, Byte.MIN_VALUE
                }
        );

        doTest(
                new Byte[]{
                        0, 1, 2, 3, 5, 7, 9, Byte.MAX_VALUE, Byte.MIN_VALUE
                }
        );

        doTest(
                new short[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Short.MAX_VALUE, Short.MIN_VALUE
                }
        );
        doTest(
                new Short[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Short.MAX_VALUE, Short.MIN_VALUE
                }
        );

        doTest(
                new int[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Integer.MAX_VALUE, Integer.MIN_VALUE
                }
        );
        doTest(
                new Integer[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Integer.MAX_VALUE, Integer.MIN_VALUE
                }
        );

        doTest(
                new long[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Long.MAX_VALUE, Long.MIN_VALUE
                }
        );
        doTest(
                new Long[]{
                        0L, 1L, 2L, 3L, 5L, 7L, 9L, 120L, Long.MAX_VALUE, Long.MIN_VALUE
                }
        );

        doTest(
                new float[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Float.MAX_VALUE, Float.MIN_VALUE
                }
        );

        doTest(
                new Float[]{
                        0F, 1F, 2F, 3F, 5F, 7F, 9F, 120F, Float.MAX_VALUE, Float.MIN_VALUE
                }
        );

        doTest(
                new double[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Double.MAX_VALUE, Double.MIN_VALUE
                }
        );
        doTest(
                new Double[]{
                        0D, 1D, 2D, 3D, 5D, 7D, 9D, 120D, Double.MAX_VALUE, Double.MIN_VALUE
                }
        );

        doTest(
                new String[]{"aa", "bb", "cc", "ddd"}
        );
    }

    private void doTest(Object value) {
        if (value != null) {
            int len = Array.getLength(value);
            assertTrue(len >= 0);
        }
        ByteBuffer bb = Struct.serialize(value);
        bb.flip();
        Object value2 = Struct.deserialize(bb);
        if (value != null) {
            if (Array.getLength(value) == 0)
                assertEquals(0, Array.getLength(value2));
            else {
                assertNotNull(value2);
                assertTrue(Array.getLength(value2) > 0);
                assertEquals(value.getClass(), value2.getClass());
                TestUtils.checkEquals(value, value2);
            }
        } else
            assertNull(value2);
    }
}
