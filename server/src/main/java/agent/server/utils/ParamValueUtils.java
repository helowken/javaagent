package agent.server.utils;

import agent.base.utils.TimeFormatUtils;

import java.util.HashMap;
import java.util.Map;

public class ParamValueUtils {
    public static final String KEY_CLASS = "class";
    public static final String KEY_INVOKE = "invoke";
    public static final String KEY_CURR_TIME_MILLIS = "currTimeMillis";
    public static final String KEY_CURR_TIME = "currTime";

    public static Map<String, Object> newParamValueMap(Object... kvs) {
        return newParamValueMap(null, null, kvs);
    }

    public static Map<String, Object> newParamValueMap(String className, String invoke, Object[] kvs) {
        Map<String, Object> rsMap = new HashMap<>();
        if (className != null)
            rsMap.put(KEY_CLASS, className);
        if (invoke != null)
            rsMap.put(KEY_INVOKE, invoke);
        rsMap.put(KEY_CURR_TIME_MILLIS, System.currentTimeMillis());
        if (kvs != null) {
            if (kvs.length % 2 != 0)
                throw new IllegalArgumentException("Invalid size of key value list: " + kvs.length);
            for (int i = 0; i < kvs.length; i += 2) {
                rsMap.put(String.valueOf(kvs[0]), kvs[1]);
            }
        }
//        logger.debug("Param value map: {}", rsMap);
        return rsMap;
    }

    public static Object formatValue(Map<String, Object> paramValues, String key, Object option) {
        if (key.equals(KEY_CURR_TIME)) {
            Long timeMillis = (Long) paramValues.get(KEY_CURR_TIME_MILLIS);
            if (timeMillis == null)
                throw new RuntimeException("No current time millis found in param values.");
            if (!(option instanceof String))
                throw new RuntimeException("Invalid time format: " + option);
            return TimeFormatUtils.format((String) option, timeMillis);
        }
        return paramValues.get(key);
    }
}
