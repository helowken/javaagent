package agent.common.struct.impl;

import agent.common.struct.BBuff;

import java.lang.reflect.Array;
import java.util.Collection;

abstract class CollectionStructField extends AbstractLinearStructField {
    CollectionStructField(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    int elementSize(Object value) {
        return computeSize(value);
    }

    @Override
    void serializeElement(BBuff bb, Object value) {
        serializeField(bb, value);
    }

    @Override
    Object deserializeElement(BBuff bb) {
        return deserializeField(bb);
    }

    @Override
    Class<?> getElementClass() {
        return Object.class;
    }

    @Override
    Object valueToArray(Object value) {
        return ((Collection) value).toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    Object arrayToValue(Object array) {
        Collection coll = newCollection();
        for (int i = 0, len = Array.getLength(array); i < len; ++i) {
            coll.add(Array.get(array, i));
        }
        return coll;
    }

    abstract Collection newCollection();
}
