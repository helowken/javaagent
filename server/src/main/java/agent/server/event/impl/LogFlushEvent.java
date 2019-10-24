package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class LogFlushEvent implements AgentEvent {
    private final String outputPath;

    public LogFlushEvent(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

}
