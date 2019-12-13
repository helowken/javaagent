package agent.server.transform;

import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.util.Collections;
import java.util.LinkedList;
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

    TransformContext(String context, Set<Class<?>> classSet, List<AgentTransformer> transformerList, int action) {
        this.context = context;
        this.classSet = Collections.unmodifiableSet(classSet);
        this.transformerList = Collections.unmodifiableList(transformerList);
        this.action = action;
    }

    public Set<Class<?>> getTargetClassSet() {
        return classSet;
    }

    public Class<?> getTargetClass() {
        return classSet.isEmpty() ? null : classSet.iterator().next();
    }

    int getAction() {
        return action;
    }

    TransformResult doTransform() {
        TransformResult result = new TransformResult(this);
        transformerList.forEach(
                transformer -> {
                    try {
                        transformer.transform(this);
                    } catch (Throwable t) {
                        result.addTransformError(t, transformer);
                    }
                }
        );

        List<ProxyRegInfo> regInfos = new LinkedList<>();
        transformerList.stream()
                .map(AgentTransformer::getProxyRegInfos)
                .forEach(regInfos::addAll);

        ProxyTransformMgr.getInstance().transform(
                regInfos,
                ClassDataRepository.getInstance()::getClassData
        ).forEach(
                proxyResult -> {
                    if (proxyResult.hasError())
                        result.addCompileError(
                                proxyResult.getTargetClass(),
                                proxyResult.getError()
                        );
                    else
                        result.addProxyResult(proxyResult);
                }
        );

        return result;
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
