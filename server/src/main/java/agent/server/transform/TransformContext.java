package agent.server.transform;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_RESET = 1;
    private final Set<DestInvoke> invokeSet;
    private final List<AgentTransformer> transformerList;
    private final int action;

    public TransformContext(Set<DestInvoke> invokeSet, List<AgentTransformer> transformerList, int action) {
        this.invokeSet = Collections.unmodifiableSet(invokeSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Collection<DestInvoke> getInvokeSet() {
        return invokeSet;
    }

    List<AgentTransformer> getTransformerList() {
        return transformerList;
    }

    @Override
    public String toString() {
        return "TransformContext{" +
                ", invokeSet=" + invokeSet +
                ", transformerList=" + transformerList +
                ", action=" + action +
                '}';
    }
}
