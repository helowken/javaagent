package agent.server.transform;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unchecked")
public class TransformerData {
    private static final int INIT_CAPACITY = 10;
    private final ThreadLocal<Object> local = new ThreadLocal<>();
    private final Map<String, Object> pvs = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> intMap = new ConcurrentHashMap<>(INIT_CAPACITY);
    private final Map<String, AtomicLong> longMap = new ConcurrentHashMap<>(INIT_CAPACITY);

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
        clearInts();
        clearLongs();
        clearKeyValues();
    }

    public AtomicInteger aInt(String key) {
        return aInt(key, 0);
    }

    public AtomicInteger aInt(String key, int initValue) {
        return intMap.computeIfAbsent(
                key,
                k -> new AtomicInteger(initValue)
        );
    }

    public AtomicLong aLong(String key) {
        return aLong(key, 0);
    }

    public AtomicLong aLong(String key, long initValue) {
        return longMap.computeIfAbsent(
                key,
                k -> new AtomicLong(initValue)
        );
    }

    public void resetInts() {
        resetInts(0);
    }

    public void resetInts(int value) {
        intMap.forEach(
                (k, v) -> v.set(value)
        );
    }

    public void resetLongs() {
        resetLongs(0);
    }

    public void resetLongs(int value) {
        longMap.forEach(
                (k, v) -> v.set(value)
        );
    }

    public void clearInts() {
        intMap.clear();
    }

    public void clearLongs() {
        longMap.clear();
    }

    public void clearKeyValues() {
        pvs.clear();
    }

    public Map<String, AtomicInteger> getIntMap() {
        return Collections.unmodifiableMap(intMap);
    }

    public Map<String, AtomicLong> getLongMap() {
        return Collections.unmodifiableMap(longMap);
    }

    public Map<String, Object> getKeyValues() {
        return Collections.unmodifiableMap(pvs);
    }
}
