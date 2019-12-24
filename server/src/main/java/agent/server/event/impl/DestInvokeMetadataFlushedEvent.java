package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class DestInvokeMetadataFlushedEvent implements AgentEvent {
    private final String path;

    public DestInvokeMetadataFlushedEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
