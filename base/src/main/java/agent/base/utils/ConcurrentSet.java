package agent.base.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSet<V> implements Set<V> {
    private static final Object dummy = new Object();
    private final Map<V, Object> map = new ConcurrentHashMap<>();

    private Collection<V> toCollection() {
        return new HashSet<>(
                map.keySet()
        );
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<V> iterator() {
        return toCollection().iterator();
    }

    @Override
    public Object[] toArray() {
        return toCollection().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return toCollection().toArray(a);
    }

    @Override
    public boolean add(V v) {
        return map.put(v, dummy) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o, dummy);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return toCollection().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean flag = false;
        for (V v : c) {
            if (add(v))
                flag = true;
        }
        return flag;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return toCollection().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean flag = false;
        for (Object v : c) {
            if (remove(v))
                flag = true;
        }
        return flag;
    }

    @Override
    public void clear() {
        map.clear();
    }

    public void computeIfAbsent(V v, Runnable runnable) {
        map.computeIfAbsent(
                v,
                key -> {
                    runnable.run();
                    return dummy;
                }
        );
    }
}
