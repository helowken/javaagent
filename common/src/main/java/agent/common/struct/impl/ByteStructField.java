package agent.common.struct.impl;


import agent.common.struct.BBuff;

class ByteStructField extends AbstractStructField {
    ByteStructField() {
        super(Byte.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Byte.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        byte v = value == null ? 0 : (byte) value;
        bb.put(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.get();
    }
}
