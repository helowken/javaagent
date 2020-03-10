package agent.server.transform;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static agent.hook.utils.App.getClassFinder;

public class TransformContext {
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_RESET = 1;
    private final String context;
    private final Set<DestInvoke> invokeSet;
    private final List<AgentTransformer> transformerList;
    private final int action;

    public TransformContext(String context, Set<DestInvoke> invokeSet, List<AgentTransformer> transformerList, int action) {
        this.context = context;
        this.invokeSet = Collections.unmodifiableSet(invokeSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.action = action;
    }

    public String getContext() {
        return context;
    }

    public ClassLoader getLoader() {
        return getClassFinder().findClassLoader(context);
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
                "context='" + context + '\'' +
                ", invokeSet=" + invokeSet +
                ", transformerList=" + transformerList +
                ", action=" + action +
                '}';
    }
}
