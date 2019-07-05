package agent.server.utils.log.binary;

import agent.base.utils.Logger;
import agent.server.utils.log.OutputWriter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinaryOutputWriter implements OutputWriter {
    private static final Logger logger = Logger.getLogger(BinaryOutputWriter.class);
    private OutputStream out;

    BinaryOutputWriter(OutputStream outputStream) {
        out = new BufferedOutputStream(outputStream);
    }

    @Override
    public void write(Object content) throws IOException {
        out.write((byte[]) content);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
