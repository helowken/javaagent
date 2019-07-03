package agent.common.struct.impl;

import java.nio.ByteBuffer;

class DoubleStructField extends AbstractStructField {
    DoubleStructField() {
        super(Double.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Double.BYTES;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        double v = value == null ? 0 : (double) value;
        bb.putDouble(v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return bb.getDouble();
    }
}
