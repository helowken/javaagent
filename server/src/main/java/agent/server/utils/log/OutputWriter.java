package agent.server.utils.log;

import java.io.Closeable;
import java.io.IOException;

public interface OutputWriter extends Closeable {
    void write(Object content) throws IOException;

    void flush() throws IOException;
}
