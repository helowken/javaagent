package agent.common.struct.impl;


import agent.common.struct.BBuff;

import static agent.common.struct.impl.StructFields.T_INT;

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
