package agent.base.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class Registry<K, V> {
    private final Map<K, V> map = new ConcurrentHashMap<>();

    public void reg(K key, V value) {
        reg(key, value, (k, v) -> new RuntimeException("Reg key exists: " + key));
    }

    public <T extends RuntimeException> void reg(K key, V value, KeyExistedErrorSupplier<K, V, T> errorSupplier) {
        map.compute(
                key,
                (k, oldValue) -> {
                    if (oldValue != null && errorSupplier != null)
                        throw errorSupplier.get(key, value);
                    return value;
                }
        );
    }

    public void unreg(K key) {
        map.remove(key);
    }

    public V regIfAbsent(K key, Function<K, V> valueSupplier) {
        return map.computeIfAbsent(key, valueSupplier);
    }

    public V get(K key) {
        return get(key, k -> new RuntimeException("No value found by key: " + k));
    }

    public V get(K key, V defaultValue) {
        V v = map.get(key);
        return v == null ? defaultValue : v;
    }

    public <T extends RuntimeException> V get(K key, NotFoundErrorSupplier<K, T> errorSupplier) {
        V v = map.get(key);
        if (v == null && errorSupplier != null)
            throw errorSupplier.get(key);
        return v;
    }

    public List<V> getValues() {
        return new ArrayList<>(
                map.values()
        );
    }

    public interface KeyExistedErrorSupplier<K, V, T extends RuntimeException> {
        T get(K key, V value);
    }

    public interface NotFoundErrorSupplier<K, T extends RuntimeException> {
        T get(K key);
    }
}
