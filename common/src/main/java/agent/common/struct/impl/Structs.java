package agent.common.struct.impl;

import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.struct.DefaultBBuff;
import agent.common.struct.StructField;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

public class Structs {
    public static PojoStruct newPojo() {
        return new PojoStruct();
    }

    public static <K, V> MapStruct<K, V> newMap() {
        return new MapStruct<>();
    }

    public static <K, V> MapStruct<K, V> newTreeMap() {
        return new MapStruct<>(
                new TreeMap<>(),
                StructFields.newTreeMap()
        );
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

    public static <K, V> byte[] serializeMap(Map<K, V> map) {
        MapStruct<K, V> mapStruct = Structs.newMap();
        mapStruct.putAll(map);
        ByteBuffer bb = BufferAllocator.allocate(
                mapStruct.bytesSize()
        );
        mapStruct.serialize(
                new DefaultBBuff(bb)
        );
        bb.flip();
        return ByteUtils.getBytes(bb);
    }

    public static <K, V> Map<K, V> deserializeMap(byte[] bs) {
        ByteBuffer bb = ByteBuffer.wrap(bs);
        MapStruct<K, V> mapStruct = Structs.newMap();
        mapStruct.deserialize(
                new DefaultBBuff(bb)
        );
        return mapStruct.getAll();
    }
}
