package agent.server.utils.log.text;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.StringParser;
import agent.server.utils.log.AbstractLogWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TextLogWriter extends AbstractLogWriter<TextLogConfig, TextLogItem> {
    private StringParser.CompiledStringExpr expr;
    private final LockObject writerLock = new LockObject();
    private volatile Writer writer;

    TextLogWriter(String logKey, TextLogConfig logConfig) {
        super(logKey, logConfig);
        expr = StringParser.compile(logConfig.getOutputFormat());
    }

    @Override
    protected boolean checkToWrite(ItemBuffer itemBuffer, TextLogItem item, long itemSize, long bufferSize, long maxBufferSize) {
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
