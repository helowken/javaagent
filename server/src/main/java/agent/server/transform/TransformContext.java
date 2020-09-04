package agent.server.transform;


import agent.invoke.DestInvoke;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransformContext {
    private final Set<DestInvoke> invokeSet;
    private final List<AgentTransformer> transformerList;

    public TransformContext(Set<DestInvoke> invokeSet, List<AgentTransformer> transformerList) {
        this.invokeSet = Collections.unmodifiableSet(invokeSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
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
                '}';
    }
}
