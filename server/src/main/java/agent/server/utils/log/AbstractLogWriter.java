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
    private static final int MAX_AVAILABLE_BUFFERS = 20;
    protected final T logConfig;
    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread writerThread;
    private final LinkedBlockingQueue<List<V>> availableBuffers = new LinkedBlockingQueue<>(MAX_AVAILABLE_BUFFERS);
    private final LockObject bufferLock = new LockObject();
    private volatile List<V> currBuffer;
    private int bufferSize = 0;
    private final boolean autoFlush;
    private final int maxBufferSize;
    private AtomicBoolean closed = new AtomicBoolean(false);

    protected AbstractLogWriter(T logConfig) {
        this.logConfig = logConfig;
        for (int i = 0; i < MAX_AVAILABLE_BUFFERS; ++i) {
            availableBuffers.add(new LinkedList<>());
        }
        autoFlush = logConfig.isAutoFlush();
        maxBufferSize = logConfig.getMaxBufferSize();
        startThread();
    }

    private void startThread() {
        writerThread = new Thread(() -> {
            while (!closed.get()) {
                try {
                    Runnable task = taskQueue.take();
                    if (closed.get())
                        break;
                    task.run();
                } catch (InterruptedException e) {
                }
            }
        });
        writerThread.start();
    }

    @Override
    public void write(V item) {
        try {
            int size = autoFlush ? 0 : computeSize(item);
            bufferLock.sync(lock -> {
                if (currBuffer == null)
                    currBuffer = availableBuffers.take();
                currBuffer.add(item);
                bufferSize += size;
                if (autoFlush || bufferSize >= maxBufferSize)
                    taskQueue.add(
                            () -> writeDirtyBuffer(
                                    getDirtyBuffer(),
                                    autoFlush
                            )
                    );
            });
        } catch (Exception e) {
            logger.error("Write failed", e);
        }
    }

    private List<V> getDirtyBuffer() {
        List<V> dirtyBuffer = currBuffer;
        currBuffer = null;
        bufferSize = 0;
        return dirtyBuffer;
    }

    private void writeDirtyBuffer(final List<V> dirtyBuffer, final boolean flush) {
        if (dirtyBuffer != null) {
            try {
                while (!dirtyBuffer.isEmpty()) {
                    doWrite(dirtyBuffer.remove(0));
                }
            } catch (Exception e) {
                logger.error("Write dirty buffer failed.", e);
            }
            if (!dirtyBuffer.isEmpty()) {
                logger.error("Buffer is still dirty, do clearing.");
                dirtyBuffer.clear();
            }
            availableBuffers.add(dirtyBuffer);
        }
        if (flush) {
            try {
                doFlush();
            } catch (Exception e) {
                logger.error("Flush failed.", e);
            }
            EventListenerMgr.fireEvent(new LogFlushedEvent(logConfig.getOutputPath()), true);
        }
    }

    @Override
    public void flush() {
        List<V> dirtyBuffer = bufferLock.syncValue(lock -> getDirtyBuffer());
        taskQueue.add(
                () -> writeDirtyBuffer(dirtyBuffer, true)
        );
    }

    @Override
    public LogConfig getConfig() {
        return logConfig;
    }

    protected OutputStream getOutputStream() throws FileNotFoundException {
        String outputPath = logConfig.getOutputPath();
        if (isStdout(outputPath)) {
            logger.debug("Output to console.");
            return System.out;
        } else {
            logger.debug("Output to {}.", outputPath);
            return new FileOutputStream(outputPath, true);
        }
    }

    private boolean isStdout(String outputPath) {
        return outputPath == null || STDOUT.equals(outputPath);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.debug("Start to close logger: {}", logConfig);
            taskQueue.add(() -> {
            });
            try {
                writerThread.join();
            } catch (InterruptedException e) {
            }
            if (!isStdout(logConfig.getOutputPath()))
                doClose();
            logger.debug("Logger closed: {}", logConfig);
        }
    }

    protected abstract int computeSize(V item);

    protected abstract void doWrite(V item) throws IOException;

    protected abstract void doFlush() throws IOException;

    protected abstract void doClose();

}
