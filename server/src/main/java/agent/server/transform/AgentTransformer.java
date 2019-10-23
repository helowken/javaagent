package agent.server.transform;

import java.util.Collection;

public interface AgentTransformer {
    void transform(TransformContext transformContext, Class<?> clazz) throws Exception;

    Collection<Class<?>> getTransformedClasses();
}
