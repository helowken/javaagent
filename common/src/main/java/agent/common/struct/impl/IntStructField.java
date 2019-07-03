package agent.common.struct.impl;

import java.nio.ByteBuffer;

class IntStructField extends AbstractStructField {
    IntStructField() {
        super(Integer.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Integer.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        int v = value == null ? 0 : (int) value;
        bb.putInt(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.getInt();
    }
}
