package test.server;

import agent.server.transform.impl.AbstractAnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.tools.asm.ProxyCallInfo;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class TestAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {
    private final Object instance;
    private final Set<Class<?>> anntClasses;

    public TestAnnotationConfigTransformer(Object instance) {
        this.instance = instance;
        this.anntClasses = Collections.singleton(
                instance.getClass()
        );
    }

    @Override
    protected Object[] getOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        return null;
    }

    @Override
    protected Object getInstanceForMethod(Method method) {
        return instance;
    }

    @Override
    protected Set<Class<?>> getAnnotationClasses() {
        return anntClasses;
    }

    @Override
    protected ProxyCallInfo.DisplayFunc getDisplayFunc() {
        return null;
    }

    @Override
    public String getRegKey() {
        return "TestAnnt";
    }
}
