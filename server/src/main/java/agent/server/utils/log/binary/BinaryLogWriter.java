package agent.server.utils.log.binary;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.server.utils.log.AbstractLogWriter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BinaryLogWriter extends AbstractLogWriter<BinaryLogConfig, BinaryLogItem> {
    private static final Logger logger = Logger.getLogger(BinaryLogItem.class);
    private static final int FLUSH_COUNT = 1000;
    private final LockObject writerLock = new LockObject();
    private volatile OutputStream out;
    private volatile FileChannel channel;

    BinaryLogWriter(String logKey, BinaryLogConfig logConfig) {
        super(logKey, logConfig);
    }

    @Override
    protected long computeSize(BinaryLogItem item) {
        return item.getSize();
    }

    @Override
    protected void checkBeforeFlush(ItemBuffer itemBuffer) {
        BinaryLogItemPool.getList(logKey, FLUSH_COUNT)
                .forEach(
                        item -> itemBuffer.add(
                                item,
                                item.getSize()
                        )
                );
    }

    @Override
    protected boolean checkToWrite(ItemBuffer itemBuffer, BinaryLogItem item, long itemSize, long currBufferSize, long maxBufferSize) {
        if (itemSize >= maxBufferSize) {
            itemBuffer.add(item, itemSize);
            return true;
        }
        BinaryLogItemPool.put(logKey, item);
        return false;
    }

    @Override
    protected void doWrite(BinaryLogItem item) throws IOException {
        if (stdout) {
            OutputStream out = getOut();
            for (ByteBuffer bb : item.getBuffers()) {
                byte[] bs = ByteUtils.getBytes(bb);
                out.write(bs, 0, bs.length);
            }
        } else {
            long size = item.getSize();
            long bytesWritten = getChannel().write(item.getBuffers());
            if (bytesWritten != size)
                logger.error("Write failed, bytes written: {}, total size: {}", bytesWritten, size);
        }
    }

    @Override
    protected void doFlush() throws IOException {
        if (stdout)
            getOut().flush();
        else
            getChannel().force(true);
    }

    @Override
    protected void doClose() {
        writerLock.sync(lock -> {
            IOUtils.close(out);
            IOUtils.close(channel);
            out = null;
            channel = null;
        });
    }

    private OutputStream getOut() {
        if (out == null) {
            writerLock.sync(lock -> {
                if (out == null)
                    out = new BufferedOutputStream(getOutputStream());
            });
        }
        return out;
    }

    private FileChannel getChannel() {
        if (channel == null) {
            writerLock.sync(lock -> {
                if (channel == null)
                    channel = ((FileOutputStream) getOutputStream()).getChannel();
            });
        }
        return channel;
    }
}
