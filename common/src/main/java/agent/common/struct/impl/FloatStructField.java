package agent.common.struct.impl;


import agent.common.struct.BBuff;

import static agent.common.struct.impl.StructFields.T_FLOAT;

class FloatStructField extends PrimitiveStructField {
    FloatStructField() {
        super(T_FLOAT, Float.class, float.class);
    }

    @Override
    int fixedSize() {
        return Float.BYTES;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.putFloat((Float) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.getFloat();
    }
}
