package agent.common.struct.impl;


import agent.common.struct.BBuff;

class ShortStructField extends AbstractStructField {
    ShortStructField() {
        super(Short.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Short.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        short v = value == null ? 0 : (short) value;
        bb.putShort(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.getShort();
    }
}
