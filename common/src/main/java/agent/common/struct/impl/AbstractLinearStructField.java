package agent.common.struct.impl;

import agent.common.struct.BBuff;

import java.lang.reflect.Array;

abstract class AbstractLinearStructField extends CompoundStructField {
    AbstractLinearStructField(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public int bytesSize(Object value) {
        Object array = valueToArray(value);
        int size = Integer.BYTES;
        if (array != null) {
            for (int i = 0, len = Array.getLength(array); i < len; ++i) {
                size += elementSize(Array.get(array, i));
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
                serializeElement(bb, Array.get(array, i));
            }
        } else {
            bb.putInt(0);
        }
    }

    @Override
    public Object deserialize(BBuff bb) {
        int len = bb.getInt();
        if (len == 0)
            return null;
        Object array = Array.newInstance(getElementClass(), len);
        for (int i = 0; i < len; ++i) {
            Array.set(array, i, deserializeElement(bb));
        }
        return arrayToValue(array);
    }

    abstract int elementSize(Object value);

    abstract void serializeElement(BBuff bb, Object value);

    abstract Object deserializeElement(BBuff bb);

    abstract Class<?> getElementClass();

    abstract Object valueToArray(Object value);

    abstract Object arrayToValue(Object array);

}
