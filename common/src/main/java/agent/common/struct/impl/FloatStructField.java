package agent.common.struct.impl;


import agent.common.struct.BBuff;

class FloatStructField extends AbstractStructField {
    FloatStructField() {
        super(Float.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Float.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        float v = value == null ? 0 : (float) value;
        bb.putFloat(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.getFloat();
    }
}
