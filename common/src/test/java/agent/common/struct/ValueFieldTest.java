package agent.common.struct;

import agent.base.struct.impl.Struct;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ValueFieldTest {
    @Test
    public void test() {
        doTest((byte) 22);
        doTest(true);
        doTest((short) 33);
        doTest(44);
        doTest(55L);
        doTest(6.6F);
        doTest(7.7);
        doTest("8888");
        doTest(null);
    }

    private void doTest(Object value) {
        ByteBuffer bb = Struct.serialize(value);
        bb.flip();
        Object value2 = Struct.deserialize(bb);
        assertEquals(value, value2);
    }
}
