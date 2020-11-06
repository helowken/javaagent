package agent.common.struct;

import java.nio.ByteBuffer;

public class DefaultBBuff implements BBuff {
    private final ByteBuffer buffer;
    private int initPos;

    public DefaultBBuff(ByteBuffer buffer) {
        this.buffer = buffer;
        this.initPos = buffer.position();
    }

    @Override
    public void put(byte v) {
        buffer.put(v);
    }

    @Override
    public byte get() {
        return buffer.get();
    }

    @Override
    public void putShort(short v) {
        buffer.putShort(v);
    }

    @Override
    public short getShort() {
        return buffer.getShort();
    }

    @Override
    public void putInt(int v) {
//        ByteUtils.writeVarInt(v, buffer);
        buffer.putInt(v);
    }

    @Override
    public int getInt() {
//        return ByteUtils.readVarInt(buffer);
        return buffer.getInt();
    }

    @Override
    public float getFloat() {
        return buffer.getFloat();
    }

    @Override
    public void putFloat(float v) {
        buffer.putFloat(v);
    }

    @Override
    public double getDouble() {
        return buffer.getDouble();
    }

    @Override
    public void putDouble(double v) {
        buffer.putDouble(v);
    }

    @Override
    public long getLong() {
        return buffer.getLong();
    }

    @Override
    public void putLong(long v) {
        buffer.putLong(v);
    }

    @Override
    public long getSize() {
        return buffer.position() - initPos;
    }
}
