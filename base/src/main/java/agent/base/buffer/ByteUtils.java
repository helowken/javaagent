package agent.base.buffer;

import java.nio.ByteBuffer;

public class ByteUtils {

    public static byte[] getBytes(ByteBuffer bb) {
        if (bb.hasArray())
            return bb.array();
        byte[] bs = new byte[bb.position()];
        bb.flip();
        bb.get(bs);
        return bs;
    }

    public static int putShort(byte[] bs, int idx, int v) {
        bs[idx++] = (byte) (v >> 8);
        bs[idx++] = (byte) v;
        return idx;
    }

    public static int putInt(byte[] bs, int idx, int v) {
        bs[idx++] = (byte) (v >> 24);
        bs[idx++] = (byte) (v >> 16);
        bs[idx++] = (byte) (v >> 8);
        bs[idx++] = (byte) v;
        return idx;
    }

    public static int putLong(byte[] bs, int idx, long v) {
        bs[idx++] = (byte) (v >> 56);
        bs[idx++] = (byte) (v >> 48);
        bs[idx++] = (byte) (v >> 40);
        bs[idx++] = (byte) (v >> 32);

        bs[idx++] = (byte) (v >> 24);
        bs[idx++] = (byte) (v >> 16);
        bs[idx++] = (byte) (v >> 8);
        bs[idx++] = (byte) v;
        return idx;
    }

    /**
     * Read an integer stored in variable-length format using zig-zag decoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html"> Google Protocol Buffers</a>.
     **/
    public static int readVarInt(ByteBuffer buffer) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = buffer.get()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 28)
                throw illegalVarintException(value);
        }
        value |= b << i;
        return (value >>> 1) ^ -(value & 1);
    }

    public static long readVarLong(ByteBuffer buffer) {
        long value = 0L;
        int i = 0;
        long b;
        while (((b = buffer.get()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 63)
                throw illegalVarlongException(value);
        }
        value |= b << i;
        return (value >>> 1) ^ -(value & 1);
    }

    public static void writeVarInt(int value, ByteBuffer buffer) {
        int v = (value << 1) ^ (value >> 31);
        while ((v & 0xffffff80) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.put(b);
            v >>>= 7;
        }
        buffer.put((byte) v);
    }

    public static void writeVarLong(long value, ByteBuffer buffer) {
        long v = (value << 1) ^ (value >> 63);
        while ((v & 0xffffffffffffff80L) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.put(b);
            v >>>= 7;
        }
        buffer.put((byte) v);
    }

    private static IllegalArgumentException illegalVarintException(int value) {
        throw new IllegalArgumentException("Varint is too long, the most significant bit in the 5th byte is set, " +
                "converted value: " + Integer.toHexString(value));
    }

    private static IllegalArgumentException illegalVarlongException(long value) {
        throw new IllegalArgumentException("Varlong is too long, most significant bit in the 10th byte is set, " +
                "converted value: " + Long.toHexString(value));
    }


}
