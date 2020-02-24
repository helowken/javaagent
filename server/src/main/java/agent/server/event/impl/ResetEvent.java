package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class ResetEvent implements AgentEvent {
    private final boolean allReset;
    private final String context;

    public ResetEvent(String context, boolean allReset) {
        this.context = context;
        this.allReset = allReset;
    }

    public boolean isAllReset() {
        return allReset;
    }

    public String getContext() {
        return context;
    }

}