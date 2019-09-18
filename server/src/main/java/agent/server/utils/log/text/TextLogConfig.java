package agent.server.utils.log.text;

import agent.server.utils.log.AbstractLogConfig;

public class TextLogConfig extends AbstractLogConfig {
    private final String outputFormat;
    private final String timeFormat;

    public TextLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, String outputFormat, String timeFormat) {
        super(outputPath, autoFlush, maxBufferSize, bufferCount);
        this.outputFormat = outputFormat;
        this.timeFormat = timeFormat;
    }

    @Override
    public String toString() {
        return "{" +
                super.toString() +
                ", outputFormat='" + outputFormat + '\'' +
                ", timeFormat='" + timeFormat + '\'' +
                '}';
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

}
