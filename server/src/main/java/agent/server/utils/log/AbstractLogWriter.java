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
    protected final String logKey;
    private final LogConfig logConfig;
    private final LinkedBlockingQueue<ItemBuffer<V>> taskQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemBuffer<V>> availableBuffers;
    private final LockObject bufferLock = new LockObject();
    private final ControlBuffer<V> controlBuffer = new ControlBuffer<>();
    private ItemBuffer<V> currBuffer;
    private Thread writerThread;
    private AtomicBoolean close = new AtomicBoolean(false);
    private long currFileSize = 0;
    private int fileIdx = 0;
    private volatile long lastWriteTimestamp = 0;

    protected AbstractLogWriter(String logKey, LogConfig logConfig) {
        this.logKey = logKey;
        this.logConfig = logConfig;
        int bufferCount = logConfig.getBufferCount();
        availableBuffers = new LinkedBlockingQueue<>(bufferCount);
        for (int i = 0; i < bufferCount; ++i) {
            availableBuffers.add(new DataBuffer());
        }
        startThread();
    }

    private void startThread() {
        writerThread = new Thread(
                () -> {
                    boolean exit;
                    while (true) {
                        ItemBuffer<V> itemBuffer = null;
                        try {
                            tryToRollFile();
                            itemBuffer = taskQueue.take();
                            exit = itemBuffer.exec();
                            if (exit)
                                break;
                        } catch (InterruptedException e) {
                            getLogger().error("Write thread is interrupted.", e);
                        } catch (Throwable e) {
                            getLogger().error("Write thread meets unknown error.", e);
                        }
                        if (itemBuffer != null)
                            itemBuffer.clear();
                        else
                            break;
                    }
                },
                Constants.AGENT_THREAD_PREFIX + "Writer"
        );
        getLogger().debug("Start writer thread: {}", logKey);
        writerThread.start();
    }

    @Override
    public boolean isIdleTimeout(long idleTimeout) {
        long t = lastWriteTimestamp;
        return t > 0 &&
                System.currentTimeMillis() - t >= idleTimeout;
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
                        addToTaskQueue(
                                getDirtyBuffer()
                        );
                    else
                        lastWriteTimestamp = System.currentTimeMillis();
                } else
                    getLogger().error("Get available buffer timeout, write queue size is: " + taskQueue.size());
            });
        } catch (Exception e) {
            getLogger().error("Write failed", e);
        }
    }

    private ItemBuffer<V> getDirtyBuffer() {
        ItemBuffer<V> dirtyBuffer = currBuffer;
        currBuffer = null;
        return dirtyBuffer;
    }

    @Override
    public void flush() {
        getLogger().debug("Flush log to: {} - {}", logKey, getOutputFileName());
        ItemBuffer<V> dirtyBuffer = bufferLock.syncValue(lock -> getDirtyBuffer());
        if (dirtyBuffer != null) {
            dirtyBuffer.markFlush();
            checkBeforeFlush(dirtyBuffer);
        } else
            dirtyBuffer = controlBuffer;
        addToTaskQueue(dirtyBuffer);
    }

    private void addToTaskQueue(ItemBuffer<V> itemBuffer) {
        taskQueue.add(itemBuffer);
    }

    protected void checkBeforeFlush(ItemBuffer<V> itemBuffer) {
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
        getLogger().debug("Output to {} - {}.", logKey, fileName);
        return new FileOutputStream(fileName);
    }

    @Override
    public void close() {
        if (close.compareAndSet(false, true)) {
            getLogger().debug("Start to close logger: {} - {}", logKey, logConfig);
            try {
                addToTaskQueue(controlBuffer);
                writerThread.join();
            } catch (Exception e) {
                getLogger().error("Write thread occurs error .", e);
            } finally {
                doClose();
            }
            getLogger().debug("Logger closed: {} - {}", logKey, logConfig);
        }
    }

    private void tryToRollFile() {
        if (logConfig.isRollFile() &&
                currFileSize >= logConfig.getRollFileSize()) {
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
            getLogger().error("Flush failed.", e);
        } finally {
            lastWriteTimestamp = 0;
            EventListenerMgr.fireEvent(
                    new LogFlushedEvent(
                            logKey,
                            logConfig.getOutputPath()
                    ),
                    true
            );
        }
    }

    protected abstract Logger getLogger();

    protected abstract boolean checkToWrite(ItemBuffer<V> itemBuffer, V item, long itemSize, long bufferSize, long maxBufferSize);

    protected abstract long computeSize(V item);

    protected abstract void doWrite(V item) throws IOException;

    protected abstract void doFlush() throws IOException;

    protected abstract void doClose();

    protected interface ItemBuffer<V> {
        void add(V item, long itemSize);

        boolean exec();

        void clear();

        long getBufferSize();

        void markFlush();
    }

    private class DataBuffer implements ItemBuffer<V> {
        private final List<V> buffer = new LinkedList<>();
        long bufferSize = 0;
        boolean flush = false;

        @Override
        public void add(V item, long itemSize) {
            buffer.add(item);
            bufferSize += itemSize;
        }

        @Override
        public boolean exec() {
            while (!buffer.isEmpty()) {
                V item = buffer.remove(0);
                try {
                    doWrite(item);
                } catch (Exception e) {
                    getLogger().error("Write dirty buffer failed.", e);
                }
                item.postWrite();
            }
            if (flush)
                flushOutput();
            currFileSize += bufferSize;
            return false;
        }

        @Override
        public void clear() {
            buffer.forEach(V::postWrite);
            buffer.clear();
            bufferSize = 0;
            flush = logConfig.isAutoFlush();
            if (!availableBuffers.offer(this)) {
                getLogger().error("Sys error for no more space to add availableBuffer.");
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

    private class ControlBuffer<T> implements ItemBuffer<T> {

        @Override
        public void add(Object item, long itemSize) {
        }

        @Override
        public boolean exec() {
            flushOutput();
            return close.get();
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
