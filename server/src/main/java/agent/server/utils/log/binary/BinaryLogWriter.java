package agent.server.utils.log.binary;

import agent.server.utils.log.AbstractLogWriter;

public class BinaryLogWriter extends AbstractLogWriter<BinaryLogConfig, byte[]> {
    BinaryLogWriter(BinaryLogConfig logConfig) {
        super(logConfig);
    }

    @Override
    protected byte[] convertContent(Object v) {
        return BinaryConverterRegistry.getConverter(v.getClass()).convert(v);
    }

    @Override
    protected int computeSize(byte[] content) {
        return content.length;
    }
}
