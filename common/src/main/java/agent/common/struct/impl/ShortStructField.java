package agent.common.struct.impl;

import java.nio.ByteBuffer;

class ShortStructField extends AbstractStructField {
    ShortStructField() {
        super(Short.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Short.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        short v = value == null ? 0 : (short) value;
        bb.putShort(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.getShort();
    }
}
