package agent.server.event.impl;

import agent.server.event.AgentEvent;
import agent.server.transform.TransformContext;

public class ResetClassEvent implements AgentEvent {
    public static final String EVENT_TYPE = ResetClassEvent.class.getSimpleName();
    private final boolean resetAll;
    private final TransformContext transformContext;

    public ResetClassEvent(TransformContext transformContext, boolean resetAll) {
        this.transformContext = transformContext;
        this.resetAll = resetAll;
    }

    public boolean isResetAll() {
        return resetAll;
    }

    public TransformContext getTransformContext() {
        return transformContext;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
