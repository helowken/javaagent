package agent.common.buffer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BufferAllocator {
    private static final int MAX_BUFFER_COUNT = 10000;
    private static final int POOL_INIT_SIZE = 100;
    private static final int BUFFER_SIZE = 4096 * 10;
    private static final Queue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    static {
        for (int i = 0; i < POOL_INIT_SIZE; ++i) {
            put(get());
        }
    }

    public static ByteBuffer allocate(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    public static ByteBuffer get() {
        ByteBuffer bb = pool.poll();
        if (bb == null)
            bb = ByteBuffer.allocate(BUFFER_SIZE);
        else
            bb.clear();
        return bb;
    }

    // no need to accurately limit the bb count
    public static void put(ByteBuffer bb) {
        if (pool.size() < MAX_BUFFER_COUNT)
            pool.add(bb);
    }

    public static void put(Collection<ByteBuffer> bbs) {
        bbs.forEach(BufferAllocator::put);
    }
}
