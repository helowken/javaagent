package agent.common.args.parse;

import java.util.*;

class Opts {
    private final Map<String, List<Object>> valueMap = new HashMap<>();

    boolean contains(String key) {
        return valueMap.containsKey(key);
    }

    void put(String key, Object value) {
        valueMap.computeIfAbsent(
                key,
                k -> new ArrayList<>()
        ).add(value);
    }

    List<Object> getList(String key) {
        List<Object> vs = valueMap.get(key);
        return vs == null ? null : Collections.unmodifiableList(vs);
    }

    int size() {
        return valueMap.size();
    }

    int sizeOf(String key) {
        return getList(key).size();
    }

    <T> T get(String key) {
        List<Object> vs = getList(key);
        return vs == null || vs.isEmpty() ? null : (T) vs.get(0);
    }
}
