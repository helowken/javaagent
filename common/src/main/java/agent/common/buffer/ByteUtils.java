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
}
