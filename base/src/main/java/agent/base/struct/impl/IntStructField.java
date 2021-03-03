package agent.base.struct.impl;


import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_INT;

class IntStructField extends PrimitiveStructField {
    IntStructField() {
        super(T_INT, Integer.class, int.class);
    }

    @Override
    int fixedSize() {
        return Integer.BYTES;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.putInt((Integer) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.getInt();
    }
}
