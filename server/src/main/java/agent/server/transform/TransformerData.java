package agent.server.transform;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unchecked")
public class TransformerData {
    private final ThreadLocal<Object> local = new ThreadLocal<>();
    private final Map<String, Object> pvs = new ConcurrentHashMap<>();

    public void setLocal(Object v) {
        local.set(v);
    }

    public <T> T getLocal() {
        return (T) local.get();
    }

    public void put(String key, Object value) {
        pvs.put(key, value);
    }

    public <T> T get(String key) {
        return (T) pvs.get(key);
    }

    public void clear() {
        local.remove();
        pvs.clear();
    }

    public AtomicInteger newAInt(String key) {
        return newAInt(key, 0);
    }

    public AtomicInteger newAInt(String key, int initValue) {
        AtomicInteger v = new AtomicInteger(initValue);
        pvs.put(key, v);
        return v;
    }

    public AtomicLong newALong(String key) {
        return newALong(key, 0);
    }

    public AtomicLong newALong(String key, long initValue) {
        AtomicLong v = new AtomicLong(initValue);
        pvs.put(key, v);
        return v;
    }

    public Map<String, Object> getKeyValues() {
        return Collections.unmodifiableMap(pvs);
    }
}
