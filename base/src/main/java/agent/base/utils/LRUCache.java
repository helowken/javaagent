package agent.base.utils;

import java.util.*;
import java.util.function.Function;

public class LRUCache<K, V> {
    private int maxSize;
    private Map<K, V> map = new LinkedHashMap<>();

    public LRUCache() {
        this(100);
        new TreeMap<>();
    }

    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void put(K key, V value) {
        map.remove(key);
        map.put(key, value);
        maintainSize();
    }

    private void maintainSize() {
        int size = map.size();
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext() && size > maxSize) {
            iter.next();
            iter.remove();
            --size;
        }
    }

    public synchronized V get(K key) {
        V value = map.remove(key);
        if (value != null)
            map.put(key, value);
        return value;
    }

    public synchronized V computeIfAbsent(K key, Function<K, V> func) {
        V value = map.computeIfAbsent(key, func);
        maintainSize();
        return value;
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        maintainSize();
    }

    public synchronized Collection<V> values() {
        return new ArrayList<>(map.values());
    }

    public synchronized void clear() {
        map.clear();
    }

}
