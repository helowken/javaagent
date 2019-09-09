package agent.server.event.impl;

import agent.server.event.AgentEvent;

import java.util.Map;

public class AdditionalTransformEvent implements AgentEvent {
    public static final String EVENT_TYPE = AdditionalTransformEvent.class.getName();
    private final String context;
    private final Map<String, byte[]> classNameToBytes;

    public AdditionalTransformEvent(String context, Map<String, byte[]> classNameToBytes) {
        this.context = context;
        this.classNameToBytes = classNameToBytes;
    }

    public String getContext() {
        return context;
    }

    public Map<String, byte[]> getClassNameToBytes() {
        return classNameToBytes;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public String toString() {
        return "AdditionalTransformEvent{" +
                "context='" + context + '\'' +
                ", classNameToBytes=" + classNameToBytes +
                '}';
    }
}
