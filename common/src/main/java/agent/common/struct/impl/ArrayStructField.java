package agent.common.struct.impl;

import agent.common.struct.StructField;

import java.nio.ByteBuffer;


class ArrayStructField extends AbstractLinearStructField {
    private final StructField field;

    ArrayStructField(byte type, Class<?> valueClass) {
        super(valueClass);
        this.field = StructFields.getField(type);
    }

    @Override
    int elementSize(Object value) {
        return field.bytesSize(value);
    }

    @Override
    void serializeElement(ByteBuffer bb, Object value) {
        field.serialize(bb, value);
    }

    @Override
    Object deserializeElement(ByteBuffer bb) {
        return field.deserialize(bb);
    }

    @Override
    Class<?> getElementClass() {
        return getValueClass().getComponentType();
    }

    @Override
    Object valueToArray(Object value) {
        return value;
    }

    @Override
    Object arrayToValue(Object array) {
        return array;
    }
}
