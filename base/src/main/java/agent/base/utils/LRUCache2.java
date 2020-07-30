package agent.base.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LRUCache2<T> {
    private static final AtomicInteger seqGen = new AtomicInteger(0);
    private final Map<String, CacheItem<T>> itemMap = new HashMap<>();
    private final Set<CacheItem<T>> itemSet = new TreeSet<>();
    private int maxSize;

    public LRUCache2(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void put(String key, T value) {
        CacheItem<T> item = itemMap.get(key);
        boolean isNew = false;
        if (item == null) {
            item = new CacheItem<>(key, value);
            itemMap.put(key, item);
            isNew = true;
        } else {
            itemSet.remove(item);
        }
        item.seq = seqGen.incrementAndGet();
        itemSet.add(item);

        if (isNew)
            keepSize();
    }

    private void keepSize() {
        int size = itemSet.size();
        Iterator<CacheItem<T>> iter = itemSet.iterator();
        while (size > maxSize && iter.hasNext()) {
            CacheItem<T> item = iter.next();
            itemMap.remove(item.key);
            iter.remove();
            --size;
        }
    }

    public synchronized T get(String key) {
        CacheItem<T> item = itemMap.get(key);
        if (item == null)
            return null;
        itemSet.remove(item);
        item.seq = seqGen.incrementAndGet();
        itemSet.add(item);
        return item.value;
    }

    public synchronized List<T> values() {
        return itemSet.stream().map(CacheItem::getValue).collect(Collectors.toList());
    }

    public synchronized int size() {
        return itemSet.size();
    }

    public synchronized void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        keepSize();
    }

    public synchronized void clear() {
        itemSet.clear();
        itemMap.clear();
    }

    private static class CacheItem<T> implements Comparable<CacheItem<T>> {
        final String key;
        final T value;
        long seq;

        private CacheItem(String key, T value) {
            this.key = key;
            this.value = value;
        }

        T getValue() {
            return value;
        }

        @Override
        public int compareTo(CacheItem<T> o) {
            long v = seq - o.seq;
            if (v > 0)
                return 1;
            else if (v < 0)
                return -1;
            return 0;
        }
    }
}
