package agent.common.struct.impl;

import java.nio.ByteBuffer;

class LongStructField extends AbstractStructField {
    LongStructField() {
        super(Long.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Long.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        long v = value == null ? 0 : (long) value;
        bb.putLong(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.getLong();
    }
}
