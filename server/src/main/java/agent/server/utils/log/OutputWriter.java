package agent.server.utils.log;

import java.io.Closeable;
import java.io.IOException;

public interface OutputWriter<T> extends Closeable {
    void write(T content) throws IOException;

    void flush() throws IOException;
}
