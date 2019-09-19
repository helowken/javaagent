package agent.common.buffer;

import java.nio.ByteBuffer;

public class BufferAllocator {

    public static ByteBuffer allocate(int capacity) {
        return ByteBuffer.allocate(capacity);
    }
}
