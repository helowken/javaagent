package agent.server.utils;

import agent.base.utils.Logger;
import agent.base.utils.TimeFormatUtils;

import java.util.HashMap;
import java.util.Map;

public class ParamValueUtils {
    public static final String KEY_CLASS = "class";
    public static final String KEY_METHOD = "method";
    public static final String KEY_CURR_TIME_MILLIS = "currTimeMillis";
    public static final String KEY_CURR_TIME = "currTime";

    private static final Logger logger = Logger.getLogger(ParamValueUtils.class);

    public static Map<String, Object> newParamValueMap(String className, String methodName, Object[] kvs) {
        Map<String, Object> rsMap = new HashMap<>();
        rsMap.put(KEY_CLASS, className);
        rsMap.put(KEY_METHOD, methodName);
        rsMap.put(KEY_CURR_TIME_MILLIS, System.currentTimeMillis());
        if (kvs != null) {
            if (kvs.length % 2 != 0)
                throw new IllegalArgumentException("Invalid bytesSize of key value list: " + kvs.length);
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

    public static String genCode(String className, String methodName, Object... kvs) {
        StringBuilder sb = new StringBuilder();
        sb.append(ParamValueUtils.class.getName())
                .append(".newParamValueMap(\"")
                .append(className)
                .append("\", \"")
                .append(methodName)
                .append("\"");
        if (kvs != null) {
            sb.append(", new Object[] {");
            for (int i = 0; i < kvs.length; ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append(convertToString(kvs[i]));
            }
            sb.append("}");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String convertToString(Object v) {
        return convertToString(v, null);
    }

    public static String convertToString(Object v, Class<?> castTypeForNull) {
        if (v == null) {
            if (castTypeForNull != null)
                return "(" + castTypeForNull.getName() + ") null";
            return "null";
        }
        return (v instanceof Number || v instanceof Boolean || v instanceof Expr) ? String.valueOf(v) : "\"" + v + "\"";
    }

    public static class Expr {
        private final String value;

        public Expr(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
