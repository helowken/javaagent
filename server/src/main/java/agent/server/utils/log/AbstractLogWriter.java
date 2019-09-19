package agent.server.utils.log;

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
import java.util.concurrent.atomic.AtomicBoolean;

import static agent.server.utils.log.LogConfig.STDOUT;

public abstract class AbstractLogWriter<T extends LogConfig, V extends LogItem> implements LogWriter<V> {
    private static final Logger logger = Logger.getLogger(AbstractLogWriter.class);
    protected final T logConfig;
    protected boolean stdout;
    private final LinkedBlockingQueue<ItemBuffer> taskQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemBuffer> availableBuffers;
    private final LockObject bufferLock = new LockObject();
    private final ItemBuffer dummyBuffer = new DummyItemBuffer();
    private ItemBuffer currBuffer;
    private Thread writerThread;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private long currFileSize = 0;
    private int fileIdx = 0;

    protected AbstractLogWriter(T logConfig) {
        this.logConfig = logConfig;
        int bufferCount = logConfig.getBufferCount();
        availableBuffers = new LinkedBlockingQueue<>(bufferCount);
        for (int i = 0; i < bufferCount; ++i) {
            availableBuffers.add(new ItemBuffer());
        }
        String outputPath = logConfig.getOutputPath();
        stdout = outputPath == null || STDOUT.equals(outputPath);
        startThread();
    }

    private void startThread() {
        writerThread = new Thread(() -> {
            while (!closed.get()) {
                try {
                    tryToRollFile();
                    taskQueue.take().write();
                } catch (InterruptedException e) {
                    logger.error("Write thread is interrupted.", e);
                }
            }
        });
        writerThread.start();
    }

    @Override
    public void write(V item) {
        try {
            long size = logConfig.isAutoFlush() ? 0 : computeSize(item);
            bufferLock.sync(lock -> {
                if (currBuffer == null)
                    currBuffer = availableBuffers.take();
                currBuffer.add(item, size);
                if (logConfig.isAutoFlush() || currBuffer.bufferSize >= logConfig.getMaxBufferSize())
                    taskQueue.add(getDirtyBuffer());
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
        ItemBuffer dirtyBuffer = bufferLock.syncValue(lock -> getDirtyBuffer());
        if (dirtyBuffer != null)
            dirtyBuffer.flush = true;
        else
            dirtyBuffer = dummyBuffer;
        taskQueue.add(dirtyBuffer);
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
        if (stdout) {
            logger.debug("Output to console.");
            return System.out;
        } else {
            String fileName = getOutputFileName();
            logger.debug("Output to {}.", fileName);
            return new FileOutputStream(fileName);
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.debug("Start to close logger: {}", logConfig);
            taskQueue.add(dummyBuffer);
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                logger.error("Write thread is interrupted.", e);
            }
            if (!stdout)
                doClose();
            logger.debug("Logger closed: {}", logConfig);
        }
    }

    private void tryToFlush() {
        if (!closed.get())
            flushOutput();
        EventListenerMgr.fireEvent(new LogFlushedEvent(logConfig.getOutputPath()), true);
    }

    private void tryToRollFile() {
        if (currFileSize >= logConfig.getRollFileSize() && !closed.get() && !stdout) {
            flushOutput();
            doClose();
            ++fileIdx;
            currFileSize = 0;
        }
    }

    private void writeItem(V item) {
        try {
            doWrite(item);
        } catch (Exception e) {
            logger.error("Write dirty buffer failed.", e);
        }
    }

    private void flushOutput() {
        try {
            doFlush();
        } catch (Exception e) {
            logger.error("Flush failed.", e);
        }
    }

    protected abstract long computeSize(V item);

    protected abstract void doWrite(V item) throws IOException;

    protected abstract void doFlush() throws IOException;

    protected abstract void doClose();

    private class ItemBuffer {
        private final List<V> buffer = new LinkedList<>();
        private long bufferSize = 0;
        boolean flush = false;

        private void add(V item, long itemSize) {
            buffer.add(item);
            bufferSize += itemSize;
        }

        private void write() {
            try {
                while (!closed.get() && !buffer.isEmpty()) {
                    V item = buffer.remove(0);
                    writeItem(item);
                    item.postWrite();
                }
                if (flush)
                    tryToFlush();
            } finally {
                currFileSize += bufferSize;
                clear();
            }
        }

        private void clear() {
            buffer.forEach(V::postWrite);
            buffer.clear();
            bufferSize = 0;
            flush = logConfig.isAutoFlush();
            release();
        }

        void release() {
            availableBuffers.add(this);
        }
    }

    private class DummyItemBuffer extends ItemBuffer {
        DummyItemBuffer() {
            super();
            flush = true;
        }

        @Override
        void release() {
        }
    }
}
