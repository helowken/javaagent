package agent.base.struct.impl;


import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_DOUBLE;

class DoubleStructField extends PrimitiveStructField {
    DoubleStructField() {
        super(T_DOUBLE, Double.class, double.class);
    }

    @Override
    int fixedSize() {
        return Double.BYTES;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.putDouble((Double) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.getDouble();
    }
}
