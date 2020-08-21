package agent.common.struct.impl;

import agent.base.utils.Pair;
import agent.common.struct.StructField;

import java.util.*;


@SuppressWarnings("unchecked")
public final class StructFields {
    static final byte T_NULL = 0;
    private static final byte T_BYTE = 1;
    private static final byte T_SHORT = 2;
    private static final byte T_INT = 3;
    private static final byte T_LONG = 4;
    private static final byte T_FLOAT = 5;
    private static final byte T_DOUBLE = 6;
    private static final byte T_BOOLEAN = 7;
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

    private static final byte T_LIST = 24;
    private static final byte T_TREE_SET = 25;
    private static final byte T_SET = 26;
    private static final byte T_TREE_MAP = 27;
    private static final byte T_MAP = 28;

    private static Map<Byte, StructField> typeToField = new TreeMap<>();

    static {
        typeToField.put(T_BYTE, newByte());
        typeToField.put(T_SHORT, newShort());
        typeToField.put(T_INT, newInt());
        typeToField.put(T_LONG, newLong());
        typeToField.put(T_FLOAT, newFloat());
        typeToField.put(T_DOUBLE, newDouble());
        typeToField.put(T_BOOLEAN, newBoolean());
        typeToField.put(T_STRING, newString());

        typeToField.put(T_BYTE_ARRAY, newByteArray());
        typeToField.put(T_BYTE_WRAPPER_ARRAY, newByteWrapperArray());
        typeToField.put(T_BOOLEAN_ARRAY, newBooleanArray());
        typeToField.put(T_BOOLEAN_WRAPPER_ARRAY, newBooleanWrapperArray());
        typeToField.put(T_SHORT_ARRAY, newShortArray());
        typeToField.put(T_SHORT_WRAPPER_ARRAY, newShortWrapperArray());
        typeToField.put(T_INT_ARRAY, newIntArray());
        typeToField.put(T_INT_WRAPPER_ARRAY, newIntWrapperArray());
        typeToField.put(T_LONG_ARRAY, newLongArray());
        typeToField.put(T_LONG_WRAPPER_ARRAY, newLongWrapperArray());
        typeToField.put(T_FLOAT_ARRAY, newFloatArray());
        typeToField.put(T_FLOAT_WRAPPER_ARRAY, newFloatWrapperArray());
        typeToField.put(T_DOUBLE_ARRAY, newDoubleArray());
        typeToField.put(T_DOUBLE_WRAPPER_ARRAY, newDoubleWrapperArray());
        typeToField.put(T_STRING_ARRAY, newStringArray());

        typeToField.put(T_LIST, newList());
        typeToField.put(T_TREE_SET, newTreeSet());
        typeToField.put(T_SET, newSet());
        typeToField.put(T_TREE_MAP, newTreeMap());
        typeToField.put(T_MAP, newMap());
    }

    private static byte detectType(Object value) {
        if (value == null)
            throw new IllegalArgumentException("Value can not be null!");
        for (Map.Entry<Byte, StructField> entry : typeToField.entrySet()) {
            if (entry.getValue().matchType(value))
                return entry.getKey();
        }
        throw new RuntimeException("Unsupported value type: " + value.getClass());
    }

    static StructField getField(byte type) {
        return Optional.ofNullable(typeToField.get(type))
                .orElseThrow(() -> new RuntimeException("Unknown type: " + type));
    }

    static StructField detectField(Object value) {
        return getField(detectType(value));
    }

    static Pair<Byte, StructField> detectTypeAndField(Object value) {
        byte type = detectType(value);
        return new Pair<>(type, getField(type));
    }

    public static StructField newByte() {
        return new ByteStructField();
    }

    public static StructField newBoolean() {
        return new BooleanStructField();
    }

    public static StructField newShort() {
        return new ShortStructField();
    }

    public static StructField newInt() {
        return new IntStructField();
    }

    public static StructField newLong() {
        return new LongStructField();
    }

    public static StructField newFloat() {
        return new FloatStructField();
    }

    public static StructField newDouble() {
        return new DoubleStructField();
    }

    public static StructField newString() {
        return new ArrayStructField(T_BYTE, String.class) {
            @Override
            Class<?> getElementClass() {
                return byte.class;
            }

            @Override
            Object valueToArray(Object value) {
                return value == null ? new byte[0] : ((String) value).getBytes();
            }

            @Override
            Object arrayToValue(Object array) {
                return new String((byte[]) array);
            }
        };
    }

    public static StructField newStringArray() {
        return new ArrayStructField(T_STRING, String[].class);
    }

    public static StructField newByteArray() {
        return new ArrayStructField(T_BYTE, byte[].class);
    }

    public static StructField newByteWrapperArray() {
        return new ArrayStructField(T_BYTE, Byte[].class);
    }

    public static StructField newBooleanArray() {
        return new ArrayStructField(T_BOOLEAN, boolean[].class);
    }

    public static StructField newBooleanWrapperArray() {
        return new ArrayStructField(T_BOOLEAN, Boolean[].class);
    }

    public static StructField newShortArray() {
        return new ArrayStructField(T_SHORT, short[].class);
    }

    public static StructField newShortWrapperArray() {
        return new ArrayStructField(T_SHORT, Short[].class);
    }

    public static StructField newIntArray() {
        return new ArrayStructField(T_INT, int[].class);
    }

    public static StructField newIntWrapperArray() {
        return new ArrayStructField(T_INT, Integer[].class);
    }

    public static StructField newLongArray() {
        return new ArrayStructField(T_LONG, long[].class);
    }

    public static StructField newLongWrapperArray() {
        return new ArrayStructField(T_LONG, Long[].class);
    }

    public static StructField newFloatArray() {
        return new ArrayStructField(T_FLOAT, float[].class);
    }

    public static StructField newFloatWrapperArray() {
        return new ArrayStructField(T_FLOAT, Float[].class);
    }

    public static StructField newDoubleArray() {
        return new ArrayStructField(T_DOUBLE, double[].class);
    }

    public static StructField newDoubleWrapperArray() {
        return new ArrayStructField(T_DOUBLE, Double[].class);
    }

    public static <T extends Collection> StructField newCollection(Class<T> valueClass, Class<? extends T> instanceClass) {
        return new CollectionStructField(valueClass) {
            @Override
            Collection newCollection() {
                try {
                    return instanceClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static StructField newList() {
        return newCollection(List.class, ArrayList.class);
    }

    public static StructField newTreeSet() {
        return newCollection(Set.class, TreeSet.class);
    }

    public static StructField newSet() {
        return newCollection(Set.class, HashSet.class);
    }

    public static MapStructField newTreeMap() {
        return new MapStructField(TreeMap.class, TreeMap::new);
    }

    public static MapStructField newMap() {
        return new MapStructField(Map.class, HashMap::new);
    }
}
