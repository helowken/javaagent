package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class LogFlushedEvent implements AgentEvent {
    public static final String EVENT_TYPE = LogFlushedEvent.class.getName();
    private final String outputPath;

    public LogFlushedEvent(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
