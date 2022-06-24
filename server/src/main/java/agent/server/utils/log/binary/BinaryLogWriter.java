package agent.server.utils.log.binary;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.utils.log.AbstractLogWriter;
import agent.server.utils.log.LogConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BinaryLogWriter extends AbstractLogWriter<BinaryLogItem> {
    private static final Logger logger = Logger.getLogger(BinaryLogWriter.class);
    private static final int FLUSH_COUNT = 1000;
    private final LockObject writerLock = new LockObject();
    private volatile FileChannel channel;

    public BinaryLogWriter(String logKey, LogConfig logConfig) {
        super(logKey, logConfig);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected long computeSize(BinaryLogItem item) {
        return item.getSize();
    }

    @Override
    protected void checkBeforeFlush(ItemBuffer<BinaryLogItem> itemBuffer) {
        BinaryLogItemPool.getList(logKey, FLUSH_COUNT)
                .forEach(
                        item -> itemBuffer.add(
                                item,
                                item.getSize()
                        )
                );
    }

    @Override
    protected boolean checkToWrite(ItemBuffer<BinaryLogItem> itemBuffer, BinaryLogItem item, long itemSize, long currBufferSize, long maxBufferSize) {
        if (itemSize >= maxBufferSize) {
            itemBuffer.add(item, itemSize);
            return true;
        }
        BinaryLogItemPool.put(logKey, item);
        return false;
    }

    @Override
    protected void doWrite(BinaryLogItem item) throws IOException {
        long size = item.getSize();
        ByteBuffer[] buffers = item.getBuffers();
        while (size > 0) {
            size -= getChannel().write(buffers);
        }
    }

    @Override
    protected void doFlush() throws IOException {
        getChannel().force(true);
    }

    @Override
    protected void doClose() {
        writerLock.sync(lock -> {
            IOUtils.close(channel);
            channel = null;
        });
    }

    private FileChannel getChannel() {
        if (channel == null) {
            writerLock.sync(lock -> {
                if (channel == null)
                    channel = ((FileOutputStream) getOutputStream()).getChannel();
                else
                    logger.debug(" Already has channel: {}", this);
            });
        }
        return channel;
    }
}
