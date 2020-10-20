package agent.common.struct.impl;


import agent.common.struct.BBuff;

class IntStructField extends PremitiveStructField {
    IntStructField() {
        super(Integer.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Integer.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        int v = value == null ? 0 : (int) value;
        bb.putInt(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.getInt();
    }

    @Override
    Class<?> getPrimitiveClass() {
        return int.class;
    }
}
