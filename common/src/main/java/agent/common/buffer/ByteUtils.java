package agent.common.buffer;

import java.nio.ByteBuffer;

public class ByteUtils {
    public static byte[] getBytes(ByteBuffer bb) {
        if (bb.hasArray())
            return bb.array();
        byte[] bs = new byte[bb.limit()];
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
}
