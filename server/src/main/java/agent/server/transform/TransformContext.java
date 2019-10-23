package agent.server.transform;

import agent.server.transform.cp.AgentClassPool;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public final String context;
    private final Set<Class<?>> classSet;
    private final List<AgentTransformer> transformerList;
    private final boolean skipRecordClass;
    private AgentClassPool cp;

    public TransformContext(String context, Class<?> clazz, AgentTransformer transformer, boolean skipRecordClass) {
        this(context, Collections.singleton(clazz), Collections.singletonList(transformer), skipRecordClass);
    }

    TransformContext(String context, Set<Class<?>> classSet, List<AgentTransformer> transformerList, boolean skipRecordClass) {
        this.context = context;
        this.classSet = Collections.unmodifiableSet(classSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.skipRecordClass = skipRecordClass;
    }

    boolean isSkipRecordClass() {
        return skipRecordClass;
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
                                    t.printStackTrace();
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
                            cp.getClassData(clazz.getName())
                    );
                } catch (Throwable t) {
                    t.printStackTrace();
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
                ", skipRecordClass=" + skipRecordClass +
                '}';
    }
}
