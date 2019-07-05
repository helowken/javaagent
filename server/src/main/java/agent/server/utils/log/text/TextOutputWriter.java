package agent.server.utils.log.text;

import agent.server.utils.log.OutputWriter;

import java.io.*;

public class TextOutputWriter implements OutputWriter {
    private Writer writer;

    TextOutputWriter(OutputStream outputStream) {
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void write(Object content) throws IOException {
        writer.write((String) content);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
