package agent.server.event.impl;

import agent.server.event.AgentEvent;

import java.util.Set;

public class ResetClassEvent implements AgentEvent {
    public static final String EVENT_TYPE = ResetClassEvent.class.getSimpleName();
    private final boolean resetAll;
    private final Set<Class<?>> classSet;

    public ResetClassEvent(Set<Class<?>> classSet, boolean resetAll) {
        this.classSet = classSet;
        this.resetAll = resetAll;
    }

    public boolean isResetAll() {
        return resetAll;
    }

    public Set<Class<?>> getClassSet() {
        return classSet;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
