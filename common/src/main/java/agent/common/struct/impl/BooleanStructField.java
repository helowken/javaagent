package agent.common.struct.impl;

import agent.common.struct.StructField;

import java.nio.ByteBuffer;

class BooleanStructField extends AbstractStructField {
    private static final StructField field = new ByteStructField();

    BooleanStructField() {
        super(Boolean.class);
    }

    @Override
    public int bytesSize(Object value) {
        return field.bytesSize(value);
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        int v = value != null && (boolean) value ? 1 : 0;
        field.serialize(bb, (byte) v);
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        return ((byte) field.deserialize(bb)) == 1;
    }
}
