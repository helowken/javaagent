package agent.base.args.parse;

import java.util.*;

public class Opts {
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

    int size() {
        return valueMap.size();
    }

    int sizeOf(String key) {
        return getList(key).size();
    }

    public List<Object> getList(String key) {
        List<Object> vs = valueMap.get(key);
        return vs == null ? null : Collections.unmodifiableList(vs);
    }

    private Object getRaw(String key) {
        List<Object> vs = getList(key);
        return vs == null || vs.isEmpty() ? null : vs.get(0);
    }

    public <T> T get(String key) {
        return (T) getRaw(key);
    }

    public <T> T getNotNull(String key, T defaultValue) {
        Object v = getRaw(key);
        return v == null ? defaultValue : (T) v;
    }

    @Override
    public String toString() {
        return valueMap.toString();
    }
}
