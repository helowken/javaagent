package agent.common.struct.impl;

import java.nio.ByteBuffer;

class FloatStructField extends AbstractStructField {
    FloatStructField() {
        super(Float.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Float.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        float v = value == null ? 0 : (float) value;
        bb.putFloat(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.getFloat();
    }
}
