package agent.server.utils.log.text;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.utils.log.AbstractLogWriter;
import agent.server.utils.log.LogConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TextLogWriter extends AbstractLogWriter<TextLogItem> {
    private static final Logger logger = Logger.getLogger(TextLogWriter.class);
    private final LockObject writerLock = new LockObject();
    private volatile Writer writer;

    public TextLogWriter(String logKey, LogConfig logConfig) {
        super(logKey, logConfig);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected boolean checkToWrite(ItemBuffer<TextLogItem> itemBuffer, TextLogItem item, long itemSize, long bufferSize, long maxBufferSize) {
        itemBuffer.add(item, itemSize);
        return itemSize + bufferSize >= maxBufferSize;
    }

    @Override
    protected long computeSize(TextLogItem item) {
        return item.getSize();
    }

    private Writer getWriter() {
        if (writer == null) {
            writerLock.sync(lock -> {
                if (writer == null)
                    writer = new BufferedWriter(new OutputStreamWriter(getOutputStream()));
            });
        }
        return writer;
    }

    @Override
    protected void doWrite(TextLogItem item) throws IOException {
        getWriter().write(item.content);
    }

    @Override
    protected void doFlush() throws IOException {
        getWriter().flush();
    }

    @Override
    protected void doClose() {
        writerLock.sync(lock -> {
            IOUtils.close(writer);
            writer = null;
        });
    }

}
