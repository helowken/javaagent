package agent.builtin.transformer.utils;

import agent.base.utils.Logger;
import agent.base.utils.StringItem;
import agent.base.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DefaultValueConverter implements ValueConverter {
    private static final Logger logger = Logger.getLogger(DefaultValueConverter.class);
    private static final String KEY_INDEX = "index";
    private static final String KEY_CLASS = "class";
    private static final String KEY_VALUE = "value";
    private static final Map<Class<?>, Function<Object, String>> classToStrFunc = new HashMap<>();

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
        String errStack;
        try {
            errStack = Utils.getErrorStackStrace(error);
        } catch (Throwable e) {
            logger.error("get error stack failed.", e);
            errStack = error.getMessage();
        }
        return convert(
                error.getClass(),
                errStack
        );
    }

    private Map<String, Object> convert(Class<?> clazz, Object value) {
        String rv;
        if (value == null)
            rv = "null";
        else {
            rv = valueToString(value);
            if (clazz.equals(String.class))
                rv = "\"" + new StringItem(rv).replaceAll("\"", "\\\"").toString() + "\"";
            else if (clazz.equals(char.class) || clazz.equals(Character.class))
                rv = "'" + rv.replace("'", "\\'") + "'";
        }

        Map<String, Object> rsMap = new HashMap<>();
        rsMap.put(KEY_CLASS, clazz.getName());
        rsMap.put(KEY_VALUE, rv);
        return rsMap;
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
