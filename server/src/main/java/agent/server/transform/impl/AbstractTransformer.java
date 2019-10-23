package agent.server.transform.impl;

import agent.server.transform.AgentTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.cp.AgentClassPool;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractTransformer implements AgentTransformer {
    private Collection<Class<?>> transformedClasses = new HashSet<>();
    private TransformContext transformContext;

    @Override
    public Collection<Class<?>> getTransformedClasses() {
        return transformedClasses;
    }

    protected void addTransformedClass(Class<?> clazz) {
        transformedClasses.add(clazz);
    }

    protected TransformContext getTransformContext() {
        return transformContext;
    }

    protected AgentClassPool getClassPool() {
        if (transformContext == null)
            throw new RuntimeException("No transform context found.");
        return transformContext.getClassPool();
    }

    @Override
    public void transform(TransformContext transformContext, Class<?> clazz) throws Exception {
        this.transformContext = transformContext;
        doTransform(clazz);
        addTransformedClass(clazz);
    }

    protected abstract void doTransform(Class<?> clazz) throws Exception;

}
