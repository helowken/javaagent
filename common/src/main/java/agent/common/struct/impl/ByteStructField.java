package agent.common.struct.impl;

import java.nio.ByteBuffer;

class ByteStructField extends AbstractStructField {
    ByteStructField() {
        super(Byte.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Byte.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        byte v = value == null ? 0 : (byte) value;
        bb.put(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.get();
    }
}
