package agent.base.struct.impl;

import java.util.*;
import java.util.function.Supplier;


@SuppressWarnings("unchecked")
public final class StructFields {
    static final int TYPE_SIZE = Byte.BYTES;
    static final int LENGTH_SIZE = Integer.BYTES;

    static final byte T_NULL = -1;

    static final byte T_BYTE = 1;
    static final byte T_SHORT = 2;
    static final byte T_INT = 3;
    static final byte T_LONG = 4;
    static final byte T_FLOAT = 5;
    static final byte T_DOUBLE = 6;
    static final byte T_BOOLEAN = 7;
    private static final byte T_STRING = 8;

    private static final byte T_BYTE_ARRAY = 9;
    private static final byte T_BYTE_WRAPPER_ARRAY = 10;
    private static final byte T_BOOLEAN_ARRAY = 11;
    private static final byte T_BOOLEAN_WRAPPER_ARRAY = 12;
    private static final byte T_SHORT_ARRAY = 13;
    private static final byte T_SHORT_WRAPPER_ARRAY = 14;
    private static final byte T_INT_ARRAY = 15;
    private static final byte T_INT_WRAPPER_ARRAY = 16;
    private static final byte T_LONG_ARRAY = 17;
    private static final byte T_LONG_WRAPPER_ARRAY = 18;
    private static final byte T_FLOAT_ARRAY = 19;
    private static final byte T_FLOAT_WRAPPER_ARRAY = 20;
    private static final byte T_DOUBLE_ARRAY = 21;
    private static final byte T_DOUBLE_WRAPPER_ARRAY = 22;
    private static final byte T_STRING_ARRAY = 23;

    static final byte T_LIST = 24;
    private static final byte T_TREE_SET = 25;
    private static final byte T_SET = 26;
    private static final byte T_COLLECTION = 27;
    private static final byte T_TREE_MAP = 28;
    private static final byte T_MAP = 29;

    private static final byte T_POJO_ARRAY = 100;
    static final byte T_POJO = 101;

    private static Map<Byte, StructField> typeToField = new TreeMap<>();
    private static Map<Class<?>, StructField> classToField = new HashMap<>();
    private static List<StructField> complexFields = new ArrayList<>();

    static {
        regBaseField(newByte());
        regBaseField(newShort());
        regBaseField(newInt());
        regBaseField(newLong());
        regBaseField(newFloat());
        regBaseField(newDouble());
        regBaseField(newBoolean());
        regBaseField(newString());

        regBaseField(newByteArray());
        regBaseField(newByteWrapperArray());
        regBaseField(newBooleanArray());
        regBaseField(newBooleanWrapperArray());
        regBaseField(newShortArray());
        regBaseField(newShortWrapperArray());
        regBaseField(newIntArray());
        regBaseField(newIntWrapperArray());
        regBaseField(newLongArray());
        regBaseField(newLongWrapperArray());
        regBaseField(newFloatArray());
        regBaseField(newFloatWrapperArray());
        regBaseField(newDoubleArray());
        regBaseField(newDoubleWrapperArray());
        regBaseField(newStringArray());

        regComplexField(T_LIST, newList());
        regComplexField(T_TREE_SET, newTreeSet());
        regComplexField(T_SET, newSet());
        regComplexField(T_COLLECTION, newCollection());
        regComplexField(T_TREE_MAP, newTreeMap());
        regComplexField(T_MAP, newMap());

        regComplexField(T_POJO_ARRAY, newPojoArray());
        regComplexField(T_POJO, newPojo());

        regFieldType(T_NULL, newNull());
    }

    private static void regBaseField(StructField field) {
        for (Class<?> valueClass : field.getValueClasses()) {
            if (classToField.containsKey(valueClass))
                throw new RuntimeException("Duplicated value class for field. Value class: " + valueClass + ", Field: " + field);
            classToField.put(valueClass, field);
        }
        regFieldType(field.getType(), field);
    }

    private static void regComplexField(byte type, StructField field) {
        if (complexFields.contains(field))
            throw new RuntimeException("Duplicated field: " + field);
        complexFields.add(field);
        regFieldType(type, field);
    }

    private static void regFieldType(byte type, StructField field) {
        if (typeToField.containsKey(type))
            throw new RuntimeException("Duplicated field type: " + type);
        typeToField.put(type, field);
    }

    static StructField getField(byte type) {
        return Optional.ofNullable(
                typeToField.get(type)
        ).orElseThrow(
                () -> new RuntimeException("Unknown type: " + type)
        );
    }

    static StructField detectField(Object value) {
        if (value == null)
            return getField(T_NULL);
        StructField field = classToField.get(value.getClass());
        if (field != null)
            return field;
        for (StructField complexField : complexFields) {
            if (complexField.matchType(value))
                return complexField;
        }
        throw new RuntimeException("Unsupported value type: " + value.getClass());
    }

    private static StructField newNull() {
        return new NullStructField();
    }

    private static StructField newByte() {
        return new ByteStructField();
    }

    private static StructField newBoolean() {
        return new BooleanStructField();
    }

    private static StructField newShort() {
        return new ShortStructField();
    }

    private static StructField newInt() {
        return new IntStructField();
    }

    private static StructField newLong() {
        return new LongStructField();
    }

    private static StructField newFloat() {
        return new FloatStructField();
    }

    private static StructField newDouble() {
        return new DoubleStructField();
    }

    private static StructField newString() {
        return new ArrayStructField(T_STRING, T_BYTE, String.class, byte.class, true) {
            @Override
            Object valueToArray(Object value) {
                return ((String) value).getBytes();
            }

            @Override
            Object arrayToValue(Object array) {
                return new String((byte[]) array);
            }
        };
    }

    private static StructField newStringArray() {
        return new ArrayStructField(T_STRING_ARRAY, T_STRING, String[].class, String.class, false);
    }

    private static StructField newByteArray() {
        return new ArrayStructField(T_BYTE_ARRAY, T_BYTE, byte[].class, true);
    }

    private static StructField newByteWrapperArray() {
        return new ArrayStructField(T_BYTE_WRAPPER_ARRAY, T_BYTE, Byte[].class, false);
    }

    private static StructField newBooleanArray() {
        return new ArrayStructField(T_BOOLEAN_ARRAY, T_BOOLEAN, boolean[].class, true);
    }

    private static StructField newBooleanWrapperArray() {
        return new ArrayStructField(T_BOOLEAN_WRAPPER_ARRAY, T_BOOLEAN, Boolean[].class, false);
    }

    private static StructField newShortArray() {
        return new ArrayStructField(T_SHORT_ARRAY, T_SHORT, short[].class, true);
    }

    private static StructField newShortWrapperArray() {
        return new ArrayStructField(T_SHORT_WRAPPER_ARRAY, T_SHORT, Short[].class, false);
    }

    private static StructField newIntArray() {
        return new ArrayStructField(T_INT_ARRAY, T_INT, int[].class, true);
    }

    private static StructField newIntWrapperArray() {
        return new ArrayStructField(T_INT_WRAPPER_ARRAY, T_INT, Integer[].class, false);
    }

    private static StructField newLongArray() {
        return new ArrayStructField(T_LONG_ARRAY, T_LONG, long[].class, true);
    }

    private static StructField newLongWrapperArray() {
        return new ArrayStructField(T_LONG_WRAPPER_ARRAY, T_LONG, Long[].class, false);
    }

    private static StructField newFloatArray() {
        return new ArrayStructField(T_FLOAT_ARRAY, T_FLOAT, float[].class, true);
    }

    private static StructField newFloatWrapperArray() {
        return new ArrayStructField(T_FLOAT_WRAPPER_ARRAY, T_FLOAT, Float[].class, false);
    }

    private static StructField newDoubleArray() {
        return new ArrayStructField(T_DOUBLE_ARRAY, T_DOUBLE, double[].class, true);
    }

    private static StructField newDoubleWrapperArray() {
        return new ArrayStructField(T_DOUBLE_WRAPPER_ARRAY, T_DOUBLE, Double[].class, false);
    }

    private static <T extends Collection> StructField newCollection() {
        return newCollection(T_COLLECTION, Collection.class, ArrayList::new);
    }

    private static <T extends Collection> StructField newCollection(Class<T> valueClass, Supplier<T> newInstanceFunc) {
        return newCollection(T_COLLECTION, valueClass, newInstanceFunc);
    }

    private static <T extends Collection> StructField newCollection(byte type, Class<T> valueClass, Supplier<T> newInstanceFunc) {
        return new CollectionStructField(type, valueClass, newInstanceFunc);
    }

    private static StructField newList() {
        return newList(ArrayList.class, ArrayList::new);
    }

    private static <T extends List> StructField newList(Class<T> clazz, Supplier<T> newInstanceFunc) {
        return newCollection(T_LIST, clazz, newInstanceFunc);
    }

    private static StructField newTreeSet() {
        return newCollection(T_TREE_SET, TreeSet.class, TreeSet::new);
    }

    private static StructField newSet() {
        return newSet(Set.class, HashSet::new);
    }

    private static <T extends Set> StructField newSet(Class<T> clazz, Supplier<T> newInstanceFunc) {
        return newCollection(T_SET, clazz, newInstanceFunc);
    }

    private static MapStructField newTreeMap() {
        return new MapStructField(T_TREE_MAP, TreeMap.class, TreeMap::new);
    }

    private static MapStructField newMap() {
        return newMap(Map.class, HashMap::new);
    }

    private static <T extends Map> MapStructField newMap(Class<T> clazz, Supplier<T> newInstanceFunc) {
        return new MapStructField(T_MAP, clazz, newInstanceFunc);
    }

    private static StructField newPojo() {
        return new PojoStructField();
    }

    private static StructField newPojoArray() {
        return new ArrayStructField(T_POJO_ARRAY, T_POJO, Object[].class, false);
    }

}
