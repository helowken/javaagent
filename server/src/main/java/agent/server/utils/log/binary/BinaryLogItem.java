package agent.server.utils.log.binary;

import agent.common.struct.BBuff;
import agent.server.utils.MemoryPool;
import agent.server.utils.log.AbstractLogItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryLogItem extends AbstractLogItem implements BBuff {
    private static final int byteSize = Byte.BYTES;
    private static final int shortSize = Short.BYTES;
    private static final int intSize = Integer.BYTES;
    private static final int longSize = Long.BYTES;
    private static final int floatSize = Float.BYTES;
    private static final int doubleSize = Double.BYTES;
    private List<ByteBuffer> bufferList = new ArrayList<>();
    private ByteBuffer currBuffer;
    private long size = 0;
    private ByteBuffer markBuffer;
    private int markPosition = -1;

    public long getSize() {
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

    public void put(byte v) {
        getBuffer(byteSize).put(v);
        updateSize(byteSize);
    }

    @Override
    public byte get() {
        throw new UnsupportedOperationException();
    }

    public void putShort(short v) {
        getBuffer(shortSize).putShort(v);
        updateSize(shortSize);
    }

    @Override
    public short getShort() {
        throw new UnsupportedOperationException();
    }

    public void putInt(int v) {
        getBuffer(intSize).putInt(v);
        updateSize(intSize);
    }

    @Override
    public int getInt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putFloat(float v) {
        getBuffer(floatSize).putFloat(v);
        updateSize(floatSize);
    }

    @Override
    public double getDouble() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDouble(double v) {
        getBuffer(doubleSize).putDouble(v);
        updateSize(doubleSize);
    }

    @Override
    public long getLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putLong(long v) {
        getBuffer(longSize).putLong(v);
        updateSize(longSize);
    }

    public void putIntToMark(int v) {
        putToMark(
                () -> markBuffer.putInt(markPosition, v)
        );
    }

    public void putLongToMark(long v) {
        putToMark(
                () -> markBuffer.putLong(markPosition, v)
        );
    }

    private void putToMark(Runnable runnable) {
        if (markBuffer == null || markPosition < 0)
            throw new RuntimeException("No mark buffer or position found.");
        runnable.run();
        markBuffer = null;
        markPosition = -1;
    }

    public void markAndPosition(int v) {
        ByteBuffer buffer = getBuffer(v);
        markBuffer = buffer;
        markPosition = markBuffer.position();
        buffer.position(markPosition + v);
        updateSize(v);
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
