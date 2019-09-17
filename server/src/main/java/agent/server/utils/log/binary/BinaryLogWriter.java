package agent.server.utils.log.binary;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.server.utils.log.AbstractLogWriter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinaryLogWriter extends AbstractLogWriter<BinaryLogConfig, BinaryLogItem> {
    private final LockObject writerLock = new LockObject();
    private volatile OutputStream out;

    BinaryLogWriter(BinaryLogConfig logConfig) {
        super(logConfig);
    }

    @Override
    protected int computeSize(BinaryLogItem item) {
        return item.len;
    }

    @Override
    protected void doWrite(BinaryLogItem item) throws IOException {
        getOut().write(item.bs, item.offset, item.len);
    }

    @Override
    protected void doFlush() throws IOException {
        getOut().flush();
    }

    @Override
    protected void doClose() {
        IOUtils.close(out);
    }

    private OutputStream getOut() {
        if (out == null) {
            writerLock.sync(lock -> {
                if (out == null)
                    out = new BufferedOutputStream(getOutputStream());
            });
        }
        return out;
    }
}
