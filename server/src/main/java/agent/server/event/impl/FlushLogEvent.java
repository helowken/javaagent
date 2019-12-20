package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class FlushLogEvent implements AgentEvent {
    private final String outputPath;

    public FlushLogEvent() {
        this(null);
    }

    public FlushLogEvent(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isFlushAll() {
        return outputPath == null;
    }

}
