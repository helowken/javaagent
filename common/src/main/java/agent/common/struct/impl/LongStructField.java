package agent.common.struct.impl;


import agent.common.struct.BBuff;

class LongStructField extends PremitiveStructField {
    LongStructField() {
        super(Long.class);
    }

    @Override
    public int bytesSize(Object value) {
        return Long.BYTES;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        long v = value == null ? 0 : (long) value;
        bb.putLong(v);
    }

    @Override
    public Object deserialize(BBuff bb) {
        return bb.getLong();
    }

    @Override
    Class<?> getPrimitiveClass() {
        return long.class;
    }
}
