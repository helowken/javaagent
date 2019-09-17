package agent.server.utils.log.text;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.StringParser;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.AbstractLogWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TextLogWriter extends AbstractLogWriter<TextLogConfig, TextLogItem> {
    private StringParser.CompiledStringExpr expr;
    private final LockObject writerLock = new LockObject();
    private volatile Writer writer;

    TextLogWriter(TextLogConfig logConfig) {
        super(logConfig);
        expr = StringParser.compile(logConfig.getOutputFormat());
    }

    @Override
    protected int computeSize(TextLogItem item) {
        item.content = expr.eval(
                item.paramValues,
                (pvs, key) -> ParamValueUtils.formatValue(
                        pvs,
                        key,
                        logConfig.getTimeFormat()
                )
        ) + "\n";
        return item.content.length();
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
        IOUtils.close(writer);
    }

}
