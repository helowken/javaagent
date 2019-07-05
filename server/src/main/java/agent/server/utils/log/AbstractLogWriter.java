package agent.server.utils.log;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractLogWriter<T extends LogConfig, V> implements LogWriter {
    protected final T logConfig;
    private final List<V> buffer = new LinkedList<>();
    private int bufferSize = 0;

    protected abstract V convertContent(Object v);

    protected abstract int computeSize(V content);

    protected AbstractLogWriter(T logConfig) {
        this.logConfig = logConfig;
    }

    @Override
    public void write(Object v, Supplier<SyncWriter> syncWriterSupplier) {
        V content = convertContent(v);
        if (!logConfig.isAutoFlush()) {
            buffer.add(content);
            bufferSize += computeSize(content);
            if (bufferSize >= logConfig.getMaxBufferSize()) {
                flush(syncWriterSupplier);
            }
        } else {
            syncWriterSupplier.get().exec(outputWriter -> outputWriter.write(content));
        }
    }

    @Override
    public void flush(Supplier<SyncWriter> syncWriterSupplier) {
        syncWriterSupplier.get().exec(outputWriter -> {
            while (!buffer.isEmpty()) {
                outputWriter.write(buffer.remove(0));
            }
            bufferSize = 0;
            outputWriter.flush();
        });
    }

    @Override
    public LogConfig getConfig() {
        return logConfig;
    }
}
