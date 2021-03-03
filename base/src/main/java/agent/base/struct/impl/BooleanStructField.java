package agent.base.struct.impl;

import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_BOOLEAN;

class BooleanStructField extends PrimitiveStructField {
    private static final ByteStructField field = new ByteStructField();

    BooleanStructField() {
        super(T_BOOLEAN, Boolean.class, boolean.class);
    }

    @Override
    int fixedSize() {
        return field.fixedSize();
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        byte v = 0;
        if ((Boolean) value)
            v = 1;
        field.serializeObject(bb, v, context);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        byte v = (Byte) field.deserializeObject(bb, context);
        return v == 1;
    }
}
