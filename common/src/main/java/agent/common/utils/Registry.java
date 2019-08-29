package agent.common.utils;

import agent.base.utils.LockObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Registry<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final LockObject mLock = new LockObject();

    public void reg(K key, V value) {
        reg(key, value, (k, v) -> new RuntimeException("Reg key exists: " + key));
    }

    public <T extends RuntimeException> void reg(K key, V value, KeyExistedErrorSupplier<K, V, T> errorSupplier) {
        mLock.sync(lock -> {
            if (map.containsKey(key) && errorSupplier != null)
                throw errorSupplier.get(key, value);
            map.put(key, value);
        });
    }

    public V get(K key) {
        return get(key, k -> new RuntimeException("No value found by key: " + k));
    }

    public <T extends RuntimeException> V get(K key, NotFoundErrorSupplier<K, T> errorSupplier) {
        V v = mLock.syncValue(lock -> map.get(key));
        if (v == null && errorSupplier != null)
            throw errorSupplier.get(key);
        return v;
    }

    public interface KeyExistedErrorSupplier<K, V, T extends RuntimeException> {
        T get(K key, V value);
    }

    public interface NotFoundErrorSupplier<K, T extends RuntimeException> {
        T get(K key);
    }
}
