package agent.builtin.transformer.utils;

import agent.base.utils.Logger;
import agent.base.utils.Pair;
import agent.base.utils.StringItem;
import agent.base.utils.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultValueConverter implements ValueConverter {
    public static final String KEY_INDEX = "index";
    public static final String KEY_CLASS = "class";
    public static final String KEY_VALUE = "value";
    private static final Logger logger = Logger.getLogger(DefaultValueConverter.class);
    private static final Map<Class<?>, Function<Object, String>> classToStrFunc = new HashMap<>();
    private static final int NULL_INDEX = 0;
    private Map<Object, Pair<Integer, String>> valueCache = new HashMap<>();
    private int valueIdx = 1;

    static {
        classToStrFunc.put(byte[].class, v -> Arrays.toString((byte[]) v));
        classToStrFunc.put(short[].class, v -> Arrays.toString((short[]) v));
        classToStrFunc.put(int[].class, v -> Arrays.toString((int[]) v));
        classToStrFunc.put(long[].class, v -> Arrays.toString((long[]) v));
        classToStrFunc.put(float[].class, v -> Arrays.toString((float[]) v));
        classToStrFunc.put(double[].class, v -> Arrays.toString((double[]) v));
        classToStrFunc.put(char[].class, v -> Arrays.toString((char[]) v));
        classToStrFunc.put(boolean[].class, v -> Arrays.toString((boolean[]) v));
    }

    public static void transformValues(List<Map<String, Object>> mapList, Map<Integer, String> valueCache) {
        if (mapList != null)
            mapList.stream()
                    .filter(Objects::nonNull)
                    .forEach(
                            map -> transformValue(map, valueCache)
                    );
    }

    public static void transformValue(Map<String, Object> map, Map<Integer, String> valueCache) {
        if (map != null) {
            Object value = map.get(KEY_VALUE);
            if (value instanceof Integer) {
                int v = (Integer) value;
                String str = null;
                if (v != NULL_INDEX) {
                    str = valueCache.get(value);
                    if (str == null)
                        str = "## Unknown value: " + value;
                }
                map.put(KEY_VALUE, str);
            }
        }
    }

    @Override
    public Map<String, Object> convertArg(int index, Class<?> clazz, Object value) {
        Map<String, Object> rsMap = convert(clazz, value);
        rsMap.put(KEY_INDEX, index);
        return rsMap;
    }

    @Override
    public Map<String, Object> convertReturnValue(Class<?> clazz, Object value) {
        return convert(clazz, value);
    }

    @Override
    public Map<String, Object> convertError(Throwable error) {
        return convert(
                error.getClass(),
                error
        );
    }

    @Override
    public Object getMetadata() {
        return valueCache.values()
                .stream()
                .collect(
                        Collectors.toMap(
                                Pair::getLeft,
                                Pair::getRight
                        )
                );
    }

    @Override
    public void destroy() {
        valueCache.clear();
    }

    private Map<String, Object> convert(Class<?> clazz, Object value) {
        Map<String, Object> rsMap = new HashMap<>();
        rsMap.put(KEY_CLASS, clazz.getName());
        rsMap.put(
                KEY_VALUE,
                getValueIndex(clazz, value)
        );
        return rsMap;
    }

    private int getValueIndex(Class<?> clazz, Object value) {
        return value == null ?
                NULL_INDEX :
                valueCache.computeIfAbsent(
                        value,
                        key -> {
                            String rv;
                            if (value instanceof Throwable) {
                                Throwable t = (Throwable) value;
                                try {
                                    rv = Utils.getErrorStackStrace(t);
                                } catch (Throwable e) {
                                    logger.warn("get error stack failed.", e);
                                    rv = t.getMessage();
                                }
                            } else {
                                rv = valueToString(value);
                                if (clazz.equals(String.class))
                                    rv = "\"" + new StringItem(rv).replaceAll("\"", "\\\"").toString() + "\"";
                                else if (clazz.equals(char.class) || clazz.equals(Character.class))
                                    rv = "'" + rv.replace("'", "\\'") + "'";
                            }
                            return new Pair<>(valueIdx++, rv);
                        }
                ).left;
    }

    public String valueToString(Object v) {
        if (v == null)
            throw new IllegalArgumentException();
        Class<?> clazz = v.getClass();
        if (clazz.isArray())
            return Optional.ofNullable(
                    classToStrFunc.get(clazz)
            ).map(
                    func -> func.apply(v)
            ).orElseGet(
                    () -> Arrays.deepToString((Object[]) v)
            );
        return String.valueOf(v);
    }
}
