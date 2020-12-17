package agent.server.utils.log;

import agent.base.utils.Constants;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.LogFlushedEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractLogWriter<V extends LogItem> implements LogWriter {
    private static final Logger logger = Logger.getLogger(AbstractLogWriter.class);
    protected final String logKey;
    private final LogConfig logConfig;
    private final LinkedBlockingQueue<ItemBuffer> taskQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemBuffer> availableBuffers;
    private final LockObject bufferLock = new LockObject();
    private final ItemBuffer dummyFlushBuffer = new DummyFlushItemBuffer();
    private ItemBuffer currBuffer;
    private Thread writerThread;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private long currFileSize = 0;
    private int fileIdx = 0;

    protected AbstractLogWriter(String logKey, LogConfig logConfig) {
        this.logKey = logKey;
        this.logConfig = logConfig;
        int bufferCount = logConfig.getBufferCount();
        availableBuffers = new LinkedBlockingQueue<>(bufferCount);
        for (int i = 0; i < bufferCount; ++i) {
            availableBuffers.add(new DefaultItemBuffer());
        }
        startThread();
    }

    private void startThread() {
        writerThread = new Thread(
                () -> {
                    while (!closed.get()) {
                        ItemBuffer itemBuffer = null;
                        try {
                            tryToRollFile();
                            itemBuffer = taskQueue.take();
                            itemBuffer.write();
                        } catch (InterruptedException e) {
                            logger.error("Write thread is interrupted.", e);
                        } catch (Throwable e) {
                            logger.error("Write thread meets unknown error.", e);
                        } finally {
                            if (itemBuffer != null)
                                itemBuffer.clear();
                        }
                    }
                },
                Constants.AGENT_THREAD_PREFIX + "Writer"
        );
        writerThread.start();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(LogItem o) {
        try {
            V item = (V) o;
            long itemSize = logConfig.isAutoFlush() ? 0 : computeSize(item);
            bufferLock.sync(lock -> {
                if (currBuffer == null) {
                    currBuffer = availableBuffers.poll(
                            logConfig.getWriteTimeoutMs(),
                            TimeUnit.MILLISECONDS
                    );
                }
                if (currBuffer != null) {
                    boolean toWrite = checkToWrite(
                            currBuffer,
                            item,
                            itemSize,
                            currBuffer.getBufferSize(),
                            logConfig.getMaxBufferSize()
                    );
                    if (logConfig.isAutoFlush() || toWrite)
                        taskQueue.add(getDirtyBuffer());
                } else
                    logger.error("Get available buffer timeout, write queue size is: " + taskQueue.size());
            });
        } catch (Exception e) {
            logger.error("Write failed", e);
        }
    }

    private ItemBuffer getDirtyBuffer() {
        ItemBuffer dirtyBuffer = currBuffer;
        currBuffer = null;
        return dirtyBuffer;
    }

    @Override
    public void flush() {
        logger.debug("Flush log to: {}", getOutputFileName());
        ItemBuffer dirtyBuffer = bufferLock.syncValue(lock -> getDirtyBuffer());
        if (dirtyBuffer != null) {
            dirtyBuffer.markFlush();
            checkBeforeFlush(dirtyBuffer);
        } else
            dirtyBuffer = dummyFlushBuffer;
        taskQueue.add(dirtyBuffer);
    }

    protected void checkBeforeFlush(ItemBuffer itemBuffer) {
    }

    @Override
    public LogConfig getConfig() {
        return logConfig;
    }

    private String getOutputFileName() {
        String outputPath = logConfig.getOutputPath();
        if (fileIdx > 0)
            outputPath += "." + fileIdx;
        return outputPath;
    }

    protected OutputStream getOutputStream() throws FileNotFoundException {
        String fileName = getOutputFileName();
        logger.debug("Output to {}.", fileName);
        return new FileOutputStream(fileName);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.debug("Start to close logger: {}", logConfig);
            taskQueue.add(dummyFlushBuffer);
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                logger.error("Write thread is interrupted.", e);
            }
            doClose();
            logger.debug("Logger closed: {}", logConfig);
        }
    }

    private void tryToFlush() {
        if (!closed.get())
            flushOutput();
    }

    private void tryToRollFile() {
        if (logConfig.isRollFile() &&
                currFileSize >= logConfig.getRollFileSize() &&
                !closed.get()) {
            flushOutput();
            doClose();
            ++fileIdx;
            currFileSize = 0;
        }
    }

    private void flushOutput() {
        try {
            doFlush();
        } catch (Exception e) {
            logger.error("Flush failed.", e);
        } finally {
            EventListenerMgr.fireEvent(
                    new LogFlushedEvent(
                            logKey,
                            logConfig.getOutputPath()
                    ),
                    true
            );
        }
    }

    protected abstract boolean checkToWrite(ItemBuffer itemBuffer, V item, long itemSize, long bufferSize, long maxBufferSize);

    protected abstract long computeSize(V item);

    protected abstract void doWrite(V item) throws IOException;

    protected abstract void doFlush() throws IOException;

    protected abstract void doClose();

    protected interface ItemBuffer<V> {
        void add(V item, long itemSize);

        void write();

        void clear();

        long getBufferSize();

        void markFlush();
    }

    private class DefaultItemBuffer implements ItemBuffer<V> {
        private final List<V> buffer = new LinkedList<>();
        long bufferSize = 0;
        boolean flush = false;

        @Override
        public void add(V item, long itemSize) {
            buffer.add(item);
            bufferSize += itemSize;
        }

        @Override
        public void write() {
            while (!closed.get() && !buffer.isEmpty()) {
                V item = buffer.remove(0);
                try {
                    doWrite(item);
                } catch (Exception e) {
                    logger.error("Write dirty buffer failed.", e);
                }
                item.postWrite();
            }
            if (flush)
                tryToFlush();
            currFileSize += bufferSize;
        }

        @Override
        public void clear() {
            buffer.forEach(V::postWrite);
            buffer.clear();
            bufferSize = 0;
            flush = logConfig.isAutoFlush();
            if (!availableBuffers.offer(this)) {
                logger.error("Sys error for no more space to add availableBuffer.");
            }
        }

        @Override
        public long getBufferSize() {
            return bufferSize;
        }

        @Override
        public void markFlush() {
            flush = true;
        }
    }

    private class DummyFlushItemBuffer implements ItemBuffer {
        @Override
        public void add(Object item, long itemSize) {
        }

        @Override
        public void write() {
            tryToFlush();
        }

        @Override
        public void clear() {
        }

        @Override
        public long getBufferSize() {
            return 0;
        }

        @Override
        public void markFlush() {
        }
    }
}
