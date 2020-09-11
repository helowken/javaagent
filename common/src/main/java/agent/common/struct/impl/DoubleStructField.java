package agent.common.struct.impl;


import agent.common.struct.BBuff;

class DoubleStructField extends AbstractStructField {
    DoubleStructField() {
        super(Double.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Double.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        double v = value == null ? 0 : (double) value;
        bb.putDouble(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.getDouble();
    }
}
