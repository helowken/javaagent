package agent.server.transform;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_RESET = 1;
    public final String context;
    private final Set<Class<?>> classSet;
    private final List<AgentTransformer> transformerList;
    private final int action;

    public TransformContext(String context, Class<?> clazz, AgentTransformer transformer, int action) {
        this(context, Collections.singleton(clazz), Collections.singletonList(transformer), action);
    }

    public TransformContext(String context, Set<Class<?>> classSet, List<AgentTransformer> transformerList, int action) {
        this.context = context;
        this.classSet = Collections.unmodifiableSet(classSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.action = action;
    }

    public String getContext() {
        return context;
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
