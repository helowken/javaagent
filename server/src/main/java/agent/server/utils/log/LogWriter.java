package agent.server.utils.log;

import java.util.function.Supplier;

public interface LogWriter {
    void write(Object content, Supplier<SyncWriter> syncWriterSupplier);

    void flush(Supplier<SyncWriter> syncWriterSupplier);

    LogConfig getConfig();
}
