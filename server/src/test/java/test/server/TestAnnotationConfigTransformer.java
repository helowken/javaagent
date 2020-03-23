package test.server;

import agent.server.transform.impl.AbstractAnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TestAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {
    public static final String REG_KEY = "TestAnnt";
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
    protected Object getInstanceForAnntMethod(Class<?> anntClass, Method method) {
        return instance;
    }

    @Override
    protected Collection<Class<?>> getAnnotationClasses() {
        return anntClasses;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

//    @Override
//    protected String newTag(DestInvoke destInvoke, Method anntMethod, int mask, int argsHint) {
//        return anntMethod.toString();
//    }
}
