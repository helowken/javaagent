package agent.server.utils.log;

import agent.base.utils.Logger;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.LogFlushedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractLogWriter<T extends LogConfig, V> implements LogWriter {
    private static final Logger logger = Logger.getLogger(AbstractLogWriter.class);
    private static final int MAX_AVAILABLE_BUFFERS = 20;
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 200;
    private static final int KEEP_ALIVE_TIME_SECONDS = 300;
    private static final int MAX_QUEUE_SIZE = 1000;
    private static final ExecutorService executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>(MAX_QUEUE_SIZE));
    protected final T logConfig;
    private final LinkedBlockingQueue<List<V>> availableBuffers = new LinkedBlockingQueue<>(MAX_AVAILABLE_BUFFERS);
    private List<V> currBuffer;
    private int bufferSize = 0;

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        executorService.shutdown();
                    } catch (Exception e) {
                        logger.error("Executor service shutdown failed.", e);
                    }
                })
        );
    }

    protected abstract V convertContent(Object v);

    protected abstract int computeSize(V content);

    protected AbstractLogWriter(T logConfig) {
        this.logConfig = logConfig;
        for (int i = 0; i < MAX_AVAILABLE_BUFFERS; ++i) {
            availableBuffers.add(new LinkedList<>());
        }
    }

    private List<V> getCurrBuffer() throws InterruptedException {
        if (currBuffer == null)
            currBuffer = availableBuffers.take();
        return currBuffer;
    }

    @Override
    public void write(Object v, Supplier<SyncWriter> syncWriterSupplier) {
        V content = convertContent(v);
        if (!logConfig.isAutoFlush()) {
            try {
                getCurrBuffer().add(content);
                bufferSize += computeSize(content);
                if (bufferSize >= logConfig.getMaxBufferSize()) {
                    flush(syncWriterSupplier);
                }
            } catch (Exception e) {
                logger.error("Write failed.", e);
            }
        } else {
            syncWriterSupplier.get().exec(outputWriter -> outputWriter.write(content));
        }
    }

    @Override
    public void flush(Supplier<SyncWriter> syncWriterSupplier) {
        if (currBuffer != null) {
            final List<V> dirtyBuffer = currBuffer;
            currBuffer = null;
            executorService.submit(() ->
                    syncWriterSupplier.get().exec(outputWriter -> {
                        try {
                            while (!dirtyBuffer.isEmpty()) {
                                outputWriter.write(dirtyBuffer.remove(0));
                            }
                            bufferSize = 0;
                            outputWriter.flush();
                        } catch (Exception e) {
                            logger.debug("Flush failed.", e);
                        } finally {
                            if (!dirtyBuffer.isEmpty()) {
                                logger.debug("Buffer is still dirty, do clearing.");
                                dirtyBuffer.clear();
                            }
                            availableBuffers.add(dirtyBuffer);
                            EventListenerMgr.fireEvent(new LogFlushedEvent(logConfig.getOutputPath()), true);
                        }
                    })
            );
        }
    }

    @Override
    public LogConfig getConfig() {
        return logConfig;
    }
}
