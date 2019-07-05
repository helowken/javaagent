package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class FlushLogEvent implements AgentEvent {
    public static final String EVENT_TYPE = FlushLogEvent.class.getSimpleName();
    private final String outputPath;

    public FlushLogEvent(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isFlushAll() {
        return outputPath == null;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
