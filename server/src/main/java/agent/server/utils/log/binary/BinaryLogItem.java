package agent.server.utils.log.binary;

import agent.base.utils.Logger;
import agent.server.utils.MemoryPool;
import agent.server.utils.log.AbstractLogItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryLogItem extends AbstractLogItem {
    private static final int intSize = Integer.BYTES;
    private List<ByteBuffer> bufferList = new ArrayList<>();
    private ByteBuffer currBuffer;
    private long size = 0;
    private ByteBuffer markBuffer;
    private int markPosition = -1;

    long getSize() {
        return size;
    }

    private void updateSize(int v) {
        size += v;
    }

    private ByteBuffer getBuffer(int size) {
        if (currBuffer == null || currBuffer.remaining() < size) {
            currBuffer = MemoryPool.get();
            bufferList.add(currBuffer);
        }
        return currBuffer;
    }

    public void putInt(int v) {
        getBuffer(intSize).putInt(v);
        updateSize(intSize);
    }

    public void putIntToMark(int v) {
        if (markBuffer == null || markPosition < 0)
            throw new RuntimeException("No mark buffer or position found.");
        markBuffer.putInt(markPosition, v);
        markBuffer = null;
        markPosition = -1;
    }

    public void markAndPosition(int v) {
        ByteBuffer buffer = getBuffer(intSize);
        markBuffer = buffer;
        markPosition = markBuffer.position();
        buffer.position(markPosition + v);
        updateSize(intSize);
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
        MemoryPool.put(bufferList);
        bufferList.clear();
        currBuffer = null;
        size = 0;
        super.postWrite();
    }
}
