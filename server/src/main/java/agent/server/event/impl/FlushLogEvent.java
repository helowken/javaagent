package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class FlushLogEvent implements AgentEvent {
    private final String key;

    public FlushLogEvent() {
        this(null);
    }

    public FlushLogEvent(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }


}
