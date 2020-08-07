package agent.common.struct;

import org.junit.Test;
import agent.common.buffer.BufferAllocator;
import agent.common.struct.StructField;
import agent.common.struct.impl.StructFields;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValueFieldTest {
    @Test
    public void testByte() {
        doTest(StructFields.newByte(), (byte) 22);
        doTest(StructFields.newBoolean(), true);
        doTest(StructFields.newShort(), (short) 33);
        doTest(StructFields.newInt(), 44);
        doTest(StructFields.newLong(), 55L);
        doTest(StructFields.newFloat(), 6.6F);
        doTest(StructFields.newDouble(), 7.7);
        doTest(StructFields.newString(), "8888");
    }

    private void doTest(StructField field, Object value) {
        ByteBuffer bb = BufferAllocator.allocate(field.bytesSize(value));
        assertTrue(field.matchType(value));
        field.serialize(bb, value);
        bb.flip();
        Object value2 = field.deserialize(bb);
        assertTrue(field.matchType(value2));
        assertEquals(value, value2);
    }
}