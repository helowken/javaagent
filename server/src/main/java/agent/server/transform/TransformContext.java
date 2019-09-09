package agent.server.transform;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransformContext {
    public final String context;
    public final Set<Class<?>> classSet;
    public final List<ErrorTraceTransformer> transformerList;
    public final boolean skipRecordClass;

    public TransformContext(String context, Class<?> clazz, ErrorTraceTransformer transformer, boolean skipRecordClass) {
        this(context, Collections.singleton(clazz), Collections.singletonList(transformer), skipRecordClass);
    }

    public TransformContext(String context, Set<Class<?>> classSet, List<ErrorTraceTransformer> transformerList, boolean skipRecordClass) {
        this.context = context;
        this.classSet = classSet;
        this.transformerList = transformerList;
        this.skipRecordClass = skipRecordClass;
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
