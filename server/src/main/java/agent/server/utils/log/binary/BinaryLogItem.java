package agent.server.utils.log.binary;

import agent.common.buffer.BufferAllocator;
import agent.server.utils.log.AbstractLogItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryLogItem extends AbstractLogItem {
    private List<ByteBuffer> bufferList = new ArrayList<>();
    private ByteBuffer currBuffer;
    private long size = -1;

    long getSize() {
        if (size == -1) {
            size = 0;
            for (ByteBuffer bb : bufferList) {
                size += bb.position();
            }
        }
        return size;
    }

    private ByteBuffer getBuffer() {
        if (currBuffer == null || !currBuffer.hasRemaining()) {
            currBuffer = BufferAllocator.get();
            bufferList.add(currBuffer);
        }
        return currBuffer;
    }

    public void putInt(int v) {
        getBuffer().putInt(v);
    }

    public void putInt(int index, int v) {
        getBuffer().putInt(index, v);
    }

    public void position(int v) {
        getBuffer().position(v);
    }

    ByteBuffer[] getBuffers() {
        ByteBuffer[] bbs = new ByteBuffer[bufferList.size()];
        for (int i = 0; i < bbs.length; ++i) {
            bbs[i] = bufferList.get(i);
            bbs[i].flip();
        }
        return bbs;
    }

    @Override
    public void postWrite() {
        BufferAllocator.put(bufferList);
        bufferList.clear();
        currBuffer = null;
        super.postWrite();
    }
}
