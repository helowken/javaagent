package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class LogFlushedEvent implements AgentEvent {
    private final String outputPath;

    public LogFlushedEvent(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

}
