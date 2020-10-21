package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.StructField;

import java.lang.reflect.Array;


class ArrayStructField extends AbstractStructField {
    private final StructField field;

    ArrayStructField(byte type, Class<?> valueClass) {
        super(valueClass);
        this.field = StructFields.getField(type);
    }

    @Override
    public int bytesSize(Object value) {
        Object array = valueToArray(value);
        int size = Integer.BYTES;
        if (array != null) {
            for (int i = 0, len = Array.getLength(array); i < len; ++i) {
                size += field.bytesSize(
                        Array.get(array, i)
                );
            }
        }
        return size;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        Object array = valueToArray(value);
        if (array != null) {
            int len = Array.getLength(array);
            bb.putInt(len);
            for (int i = 0; i < len; ++i) {
                field.serialize(
                        bb,
                        Array.get(array, i)
                );
            }
        } else
            bb.putInt(StructFields.NULL);
    }

    @Override
    public Object deserialize(BBuff bb) {
        int len = bb.getInt();
        if (len == StructFields.NULL)
            return null;
        Object array = Array.newInstance(getElementClass(), len);
        for (int i = 0; i < len; ++i) {
            Array.set(
                    array,
                    i,
                    field.deserialize(bb)
            );
        }
        return arrayToValue(array);
    }

    Class<?> getElementClass() {
        return getValueClass().getComponentType();
    }

    Object valueToArray(Object value) {
        return value;
    }

    Object arrayToValue(Object array) {
        return array;
    }
}
