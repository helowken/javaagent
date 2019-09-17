package agent.server.utils.log.binary;

import agent.server.utils.log.LogItem;

public class BinaryLogItem implements LogItem {
    final byte[] bs;
    final int offset;
    final int len;

    public BinaryLogItem(byte[] bs, int offset, int len) {
        this.bs = bs;
        this.offset = offset;
        this.len = len;
    }
}
