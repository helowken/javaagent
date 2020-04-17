package agent.server.event.impl;

import agent.server.event.AgentEvent;

public class ResetEvent implements AgentEvent {
    private final boolean allReset;

    public ResetEvent(boolean allReset) {
        this.allReset = allReset;
    }

    public boolean isAllReset() {
        return allReset;
    }

}
