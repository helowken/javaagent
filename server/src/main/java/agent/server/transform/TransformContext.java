package agent.server.transform;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public final String context;
    final Set<Class<?>> classSet;
    final List<ErrorTraceTransformer> transformerList;
    final boolean skipRecordClass;
    private Set<Class<?>> refClassSet;

    public TransformContext(String context, Class<?> clazz, ErrorTraceTransformer transformer, boolean skipRecordClass) {
        this(context, Collections.singleton(clazz), Collections.singletonList(transformer), skipRecordClass);
    }

    public TransformContext(String context, Set<Class<?>> classSet, List<ErrorTraceTransformer> transformerList, boolean skipRecordClass) {
        this.context = context;
        this.classSet = classSet;
        this.refClassSet = classSet;
        this.transformerList = transformerList;
        this.skipRecordClass = skipRecordClass;
    }

    public Set<Class<?>> getRefClassSet() {
        return refClassSet;
    }

    public void setRefClassSet(Set<Class<?>> refClassSet) {
        this.refClassSet = refClassSet;
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
