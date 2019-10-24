package agent.server.event.impl;

import agent.server.event.AgentEvent;

import java.util.Set;

public class TransformClassEvent implements AgentEvent {
    private final String context;
    private final int action;
    private final Set<Class<?>> transformedClassSet;

    public TransformClassEvent(String context, int action, Set<Class<?>> transformedClassSet) {
        this.context = context;
        this.action = action;
        this.transformedClassSet = transformedClassSet;
    }

    public String getContext() {
        return context;
    }

    public int getAction() {
        return action;
    }

    public Set<Class<?>> getTransformedClassSet() {
        return transformedClassSet;
    }

}
