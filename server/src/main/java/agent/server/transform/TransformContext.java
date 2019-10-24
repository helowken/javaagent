package agent.server.transform;

import agent.server.transform.cp.AgentClassPool;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_RESET = 1;
    public final String context;
    private final Set<Class<?>> classSet;
    private final List<AgentTransformer> transformerList;
    private final int action;
    private AgentClassPool cp;

    public TransformContext(String context, Class<?> clazz, AgentTransformer transformer, int action) {
        this(context, Collections.singleton(clazz), Collections.singletonList(transformer), action);
    }

    TransformContext(String context, Set<Class<?>> classSet, List<AgentTransformer> transformerList, int action) {
        this.context = context;
        this.classSet = Collections.unmodifiableSet(classSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    TransformResult doTransform() {
        TransformResult result = new TransformResult(this);
        cp = new AgentClassPool(context);
        try {
            transformerList.forEach(
                    transformer -> classSet.forEach(
                            clazz -> {
                                try {
                                    transformer.transform(this, clazz);
                                } catch (Throwable t) {
                                    result.addTransformError(clazz, t, transformer);
                                }
                            }
                    )
            );
            Set<Class<?>> transformedClasses = new HashSet<>();
            transformerList.forEach(
                    transformer -> transformedClasses.addAll(
                            transformer.getTransformedClasses()
                    )
            );
            transformedClasses.forEach(clazz -> {
                try {
                    result.saveClassData(
                            clazz,
                            cp.getClassData(clazz)
                    );
                } catch (Throwable t) {
                    result.addCompileError(clazz, t);
                }
            });
        } finally {
            cp.clear();
            cp = null;
        }
        return result;
    }

    public AgentClassPool getClassPool() {
        return cp;
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
