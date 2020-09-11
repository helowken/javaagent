package agent.common.struct;

import agent.common.buffer.BufferAllocator;
import agent.common.struct.impl.StructFields;
import org.junit.Test;
import utils.TestUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class ArrayStructFieldTest {
    @Test
    public void test() {
        doTest(StructFields.newBooleanArray(), null);
        doTest(StructFields.newBooleanArray(), new boolean[0]);
        doTest(StructFields.newBooleanWrapperArray(), new Boolean[0]);

        doTest(StructFields.newBooleanArray(),
                new boolean[]{
                        true, false, false, true
                }
        );
        doTest(StructFields.newBooleanWrapperArray(),
                new Boolean[]{
                        true, false, false, true
                }
        );

        doTest(StructFields.newByteArray(),
                new byte[]{
                        0, 1, 2, 3, 5, 7, 9, Byte.MAX_VALUE, Byte.MIN_VALUE
                }
        );

        doTest(StructFields.newByteWrapperArray(),
                new Byte[]{
                        0, 1, 2, 3, 5, 7, 9, Byte.MAX_VALUE, Byte.MIN_VALUE
                }
        );

        doTest(StructFields.newShortArray(),
                new short[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Short.MAX_VALUE, Short.MIN_VALUE
                }
        );
        doTest(StructFields.newShortWrapperArray(),
                new Short[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Short.MAX_VALUE, Short.MIN_VALUE
                }
        );

        doTest(StructFields.newIntArray(),
                new int[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Integer.MAX_VALUE, Integer.MIN_VALUE
                }
        );
        doTest(StructFields.newIntWrapperArray(),
                new Integer[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Integer.MAX_VALUE, Integer.MIN_VALUE
                }
        );

        doTest(StructFields.newLongArray(),
                new long[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Long.MAX_VALUE, Long.MIN_VALUE
                }
        );
        doTest(StructFields.newLongWrapperArray(),
                new Long[]{
                        0L, 1L, 2L, 3L, 5L, 7L, 9L, 120L, Long.MAX_VALUE, Long.MIN_VALUE
                }
        );

        doTest(StructFields.newFloatArray(),
                new float[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Float.MAX_VALUE, Float.MIN_VALUE
                }
        );

        doTest(StructFields.newFloatWrapperArray(),
                new Float[]{
                        0F, 1F, 2F, 3F, 5F, 7F, 9F, 120F, Float.MAX_VALUE, Float.MIN_VALUE
                }
        );

        doTest(StructFields.newDoubleArray(),
                new double[]{
                        0, 1, 2, 3, 5, 7, 9, 120, Double.MAX_VALUE, Double.MIN_VALUE
                }
        );
        doTest(StructFields.newDoubleWrapperArray(),
                new Double[]{
                        0D, 1D, 2D, 3D, 5D, 7D, 9D, 120D, Double.MAX_VALUE, Double.MIN_VALUE
                }
        );

        doTest(StructFields.newStringArray(),
                new String[]{"aa", "bb", "cc", "ddd"}
        );
    }

    private void doTest(StructField field, Object value) {
        if (value != null) {
            assertTrue(field.matchType(value));
            int len = Array.getLength(value);
            assertTrue(len >= 0);
        }
        ByteBuffer bb = BufferAllocator.allocate(field.bytesSize(value));
        BBuff buff = new DefaultBBuff(bb);
        field.serialize(buff, value);
        bb.flip();
        Object value2 = field.deserialize(buff);
        if (value != null) {
            if (Array.getLength(value) == 0)
                assertNull(value2);
            else {
                assertNotNull(value2);
                assertTrue(Array.getLength(value2) > 0);
                assertTrue(field.matchType(value2));
                assertEquals(value.getClass(), value2.getClass());
                TestUtils.checkEquals(value, value2);
            }
        } else
            assertNull(value2);
    }
}
