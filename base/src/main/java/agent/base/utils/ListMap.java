package agent.base.utils;

import java.util.*;
import java.util.stream.Collectors;

public class ListMap<K, V> implements Map<K, V> {
    private List<K> keyList = new ArrayList<>();
    private Map<K, V> map = new HashMap<>();

    @Override
    public int size() {
        return keyList.size();
    }

    @Override
    public boolean isEmpty() {
        return keyList.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (!map.containsKey(key))
            keyList.add(key);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        keyList.remove(key);
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        keyList.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keyList);
    }

    @Override
    public Collection<V> values() {
        return keyList.stream()
                .map(this::get)
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
