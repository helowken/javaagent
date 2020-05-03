package test.base;

import agent.base.utils.LRUCache;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LRUCacheTest {
    @Test
    public void test() {
        final int size = 5;
        LRUCache<String, Integer> cache = new LRUCache<>(size);
        for (int i = 0; i < 10; ++i) {
            cache.put("" + i, i);
        }
        assertEquals(size, cache.size());
        assertEquals(Arrays.asList(5, 6, 7, 8, 9), cache.values());

        cache.clear();
        for (int i = 0; i < 10; ++i) {
            final int v = i;
            cache.computeIfAbsent("" + i, k -> v);
        }
        assertEquals(size, cache.size());
        assertEquals(Arrays.asList(5, 6, 7, 8, 9), cache.values());

        cache.setMaxSize(2);
        assertEquals(2, cache.size());
        assertEquals(Arrays.asList(8, 9), cache.values());

        cache.setMaxSize(5);
        for (int i = 0; i < 5; ++i) {
            cache.put("" + i, i);
        }
        for (int i = 4; i >= 0; --i) {
            cache.put("" + i, i);
        }
        assertEquals(5, cache.size());
        assertEquals(Arrays.asList(4, 3, 2, 1, 0), cache.values());
    }
}
