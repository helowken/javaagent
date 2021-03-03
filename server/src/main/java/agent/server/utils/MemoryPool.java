package agent.server.utils;

import agent.base.buffer.BufferAllocator;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MemoryPool {
    private static final int MAX_BUFFER_COUNT = 10000;
    private static final int POOL_INIT_SIZE = 100;
    private static final int BUFFER_SIZE = 4096;
    private static final Queue<ByteBuffer> pool = new LinkedBlockingQueue<>(MAX_BUFFER_COUNT);

    static {
        for (int i = 0; i < POOL_INIT_SIZE; ++i) {
            put(allocate());
        }
    }

    private static ByteBuffer allocate() {
        return BufferAllocator.allocate(BUFFER_SIZE);
    }

    public static ByteBuffer get() {
        ByteBuffer bb = pool.poll();
        if (bb == null)
            bb = allocate();
        else
            bb.clear();
        return bb;
    }

    public static void put(ByteBuffer bb) {
        // If queue is full, just ignore it
        pool.offer(bb);
    }

    public static void put(Collection<ByteBuffer> bbs) {
        // queue status may change between full and have space during this loop
        bbs.forEach(MemoryPool::put);
    }
}
