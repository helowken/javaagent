package agent.common.struct.impl;

import agent.common.struct.StructField;

public class Structs {
    public static EmptyStruct empty() {
        return EmptyStruct.getInstance();
    }

    public static <K, V> MapStruct<K, V> newMap() {
        return new MapStruct<>();
    }

    public static <V> ListStruct<V> newList() {
        return new ListStruct<>();
    }

    public static <V> SetStruct<V> newSet() {
        return new SetStruct<>();
    }

    private static DefaultStruct create(StructField field) {
        return new DefaultStruct(field);
    }

    public static DefaultStruct newByte() {
        return create(StructFields.newByte());
    }

    public static DefaultStruct newBoolean() {
        return create(StructFields.newBoolean());
    }

    public static DefaultStruct newShort() {
        return create(StructFields.newShort());
    }

    public static DefaultStruct newInt() {
        return create(StructFields.newInt());
    }

    public static DefaultStruct newLong() {
        return create(StructFields.newLong());
    }

    public static DefaultStruct newFloat() {
        return create(StructFields.newFloat());
    }

    public static DefaultStruct newDouble() {
        return create(StructFields.newDouble());
    }

    public static DefaultStruct newString() {
        return create(StructFields.newString());
    }

    public static DefaultStruct newByteArray() {
        return create(StructFields.newByteArray());
    }

    public static DefaultStruct newBooleanArray() {
        return create(StructFields.newBooleanArray());
    }

    public static DefaultStruct newShortArray() {
        return create(StructFields.newShortArray());
    }

    public static DefaultStruct newIntArray() {
        return create(StructFields.newIntArray());
    }

    public static DefaultStruct newLongArray() {
        return create(StructFields.newLongArray());
    }

    public static DefaultStruct newFloatArray() {
        return create(StructFields.newFloatArray());
    }

    public static DefaultStruct newDoubleArray() {
        return create(StructFields.newDoubleArray());
    }

    public static DefaultStruct newStringArray() {
        return create(StructFields.newStringArray());
    }

    public static DefaultStruct newByteWrapperArray() {
        return create(StructFields.newByteWrapperArray());
    }

    public static DefaultStruct newBooleanWrapperArray() {
        return create(StructFields.newBooleanWrapperArray());
    }

    public static DefaultStruct newShortWrapperArray() {
        return create(StructFields.newShortWrapperArray());
    }

    public static DefaultStruct newIntWrapperArray() {
        return create(StructFields.newIntWrapperArray());
    }

    public static DefaultStruct newLongWrapperArray() {
        return create(StructFields.newLongWrapperArray());
    }

    public static DefaultStruct newFloatWrapperArray() {
        return create(StructFields.newFloatWrapperArray());
    }

    public static DefaultStruct newDoubleWrapperArray() {
        return create(StructFields.newDoubleWrapperArray());
    }
}
