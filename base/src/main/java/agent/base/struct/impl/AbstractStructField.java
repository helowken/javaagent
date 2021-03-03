package agent.base.struct.impl;

import agent.base.struct.BBuff;

import java.util.Arrays;

import static agent.base.struct.impl.StructFields.TYPE_SIZE;
import static agent.base.struct.impl.StructFields.T_NULL;

abstract class AbstractStructField implements StructField {
    private final byte type;
    private final Class<?>[] valueClasses;

    AbstractStructField(byte type, Class<?>... valueClasses) {
        if (valueClasses == null || valueClasses.length == 0)
            throw new IllegalArgumentException("Value classes is null or empty.");
        this.type = type;
        this.valueClasses = Arrays.copyOf(valueClasses, valueClasses.length);
    }

    @Override
    public byte getType() {
        return type;
    }

    @Override
    public boolean matchType(Object value) {
        for (Class<?> valueClass : valueClasses) {
            if (valueClass.isInstance(value))
                return true;
        }
        return false;
    }

    @Override
    public Class<?>[] getValueClasses() {
        return valueClasses;
    }

    @Override
    public int bytesSize(Object value, StructContext context) {
        if (value == null)
            return StructFields.getField(T_NULL).bytesSize(null, context);
        else {
            checkType(value);
            int size = TYPE_SIZE;
            size += sizeOf(value, context);
            return size;
        }
    }

    @Override
    public void serialize(BBuff bb, Object value, StructContext context) {
        if (value == null)
            StructFields.getField(T_NULL).serialize(bb, null, context);
        else {
            checkType(value);
            bb.put(type);
            serializeObject(bb, value, context);
        }
    }

    @Override
    public Object deserialize(BBuff bb, StructContext context) {
        Object value = deserializeObject(bb, context);
        if (value != null)
            checkType(value);
        return value;
    }

    private void checkType(Object value) {
        if (!matchType(value))
            throw new RuntimeException("Wrong type! Type: " + type + ", Classes: " + Arrays.toString(valueClasses) + ", Value: " + value);
    }

    abstract int sizeOf(Object value, StructContext context);

    abstract void serializeObject(BBuff bb, Object value, StructContext context);

    abstract Object deserializeObject(BBuff bb, StructContext context);
}
