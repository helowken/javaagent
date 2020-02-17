package agent.server.transform;

import java.util.*;

import static agent.hook.utils.App.getClassFinder;

public class TransformContext {
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_RESET = 1;
    public final String context;
    private final Set<Class<?>> classSet;
    private final List<AgentTransformer> transformerList;
    private final int action;

    public TransformContext(String context, Collection<Class<?>> classSet, List<AgentTransformer> transformerList, int action) {
        this.context = context;
        this.classSet = new HashSet<>(classSet);
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

    public Set<Class<?>> getTargetClassSet() {
        return classSet;
    }

    public Class<?> getTargetClass() {
        return classSet.isEmpty() ? null : classSet.iterator().next();
    }

    List<AgentTransformer> getTransformerList() {
        return transformerList;
    }

    @Override
    public String toString() {
        return "TransformContext{" +
                "context='" + context + '\'' +
                ", classSet=" + classSet +
                ", transformerList=" + transformerList +
                ", action=" + action +
                '}';
    }
}
