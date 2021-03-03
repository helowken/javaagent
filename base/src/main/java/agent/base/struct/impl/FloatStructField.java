package agent.base.struct.impl;


import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_FLOAT;

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
