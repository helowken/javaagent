package agent.builtin.transformer.utils;

import java.util.HashMap;
import java.util.Map;

public class DefaultMethodPrinter implements ValueConverter {
    private static final String KEY_INDEX = "index";
    private static final String KEY_CLASS = "class";
    private static final String KEY_VALUE = "value";

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
                error.getMessage()
        );
    }

    private Map<String, Object> convert(Class<?> clazz, Object value) {
        Map<String, Object> rsMap = new HashMap<>();
        rsMap.put(KEY_CLASS, clazz.getName());
        if (value != null)
            rsMap.put(KEY_VALUE, value.toString());
        return rsMap;
    }

}
