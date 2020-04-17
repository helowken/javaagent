package agent.server.event.impl;

import agent.server.event.AgentEvent;

import java.util.Set;

public class TransformClassEvent implements AgentEvent {
    private final int action;
    private final Set<Class<?>> transformedClassSet;

    public TransformClassEvent(int action, Set<Class<?>> transformedClassSet) {
        this.action = action;
        this.transformedClassSet = transformedClassSet;
    }

    public int getAction() {
        return action;
    }

    public Set<Class<?>> getTransformedClassSet() {
        return transformedClassSet;
    }

}
