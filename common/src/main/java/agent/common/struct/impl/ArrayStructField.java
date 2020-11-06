package agent.common.struct.impl;

import agent.base.utils.Pair;
import agent.common.struct.BBuff;
import agent.common.struct.StructField;

import java.lang.reflect.Array;

import static agent.common.struct.impl.StructFields.LENGTH_SIZE;


class ArrayStructField extends AbstractStructField {
    private final byte elementType;
    private final Class<?> elementClass;
    private final boolean primitive;

    ArrayStructField(byte type, byte elementType, Class<?> valueClass, boolean primitive) {
        this(type, elementType, valueClass, valueClass.getComponentType(), primitive);
    }

    ArrayStructField(byte type, byte elementType, Class<?> valueClass, Class<?> elementClass, boolean primitive) {
        super(type, valueClass);
        this.elementType = elementType;
        this.elementClass = elementClass;
        this.primitive = primitive;
    }

    private PrimitiveStructField getPrimitiveField() {
        return (PrimitiveStructField) StructFields.getField(elementType);
    }

    private void test(BBuff bb, Object value, StructContext context) {
        Pair<Class<?>, Integer> p = calculateDimension(
                value.getClass()
        );
        if (p == null)
            throw new RuntimeException("Not an array: " + value);

//        StructField field = StructFields.detectType(p.left);
//        field.serializeArray(bb, p.right);

//        context.createPojo()
    }

    private Pair<Class<?>, Integer> calculateDimension(Class<?> clazz) {
        int count = 0;
        while (true) {
            if (clazz.isArray()) {
                clazz = clazz.getComponentType();
                ++count;
            } else
                return count == 0 ? null : new Pair<>(clazz, count);
        }
    }

    @Override
    int sizeOf(Object value, StructContext context) {
        Object array = valueToArray(value);
        int size = 0;
        if (array != null) {
            size += LENGTH_SIZE;
            int len = Array.getLength(array);
            if (len > 0) {
                if (primitive) {
                    size += getPrimitiveField().fixedSize() * len;
                } else {
                    Object v;
                    for (int i = 0; i < len; ++i) {
                        v = Array.get(array, i);
                        size += Struct.bytesSize(v, context);
                    }
                }
            }
        }
        return size;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        Object array = valueToArray(value);
        if (array != null) {
            int len = Array.getLength(array);
            bb.putInt(len);
            if (len > 0) {
                if (primitive) {
                    PrimitiveStructField field = getPrimitiveField();
                    for (int i = 0; i < len; ++i) {
                        field.serializeObject(
                                bb,
                                Array.get(array, i),
                                context
                        );
                    }
                } else {
                    for (int i = 0; i < len; ++i) {
                        Struct.serialize(
                                bb,
                                Array.get(array, i),
                                context
                        );
                    }
                }
            }
        }
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        int len = bb.getInt();
        Object array = Array.newInstance(elementClass, len);
        if (primitive) {
            PrimitiveStructField field = getPrimitiveField();
            for (int i = 0; i < len; ++i) {
                Array.set(
                        array,
                        i,
                        field.deserializeObject(bb, context)
                );
            }
        } else {
            for (int i = 0; i < len; ++i) {
                Array.set(
                        array,
                        i,
                        Struct.deserialize(bb, context)
                );
            }
        }
        return arrayToValue(array);
    }

    Object valueToArray(Object value) {
        return value;
    }

    Object arrayToValue(Object array) {
        return array;
    }
}
