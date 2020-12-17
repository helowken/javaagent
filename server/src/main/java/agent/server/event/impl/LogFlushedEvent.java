package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class LogFlushedEvent implements AgentEvent {
    private final String logKey;
    private final String outputPath;

    public LogFlushedEvent(String logKey, String outputPath) {
        this.logKey = logKey;
        this.outputPath = outputPath;
    }

    public String getLogKey() {
        return logKey;
    }

    public String getOutputPath() {
        return outputPath;
    }

}
