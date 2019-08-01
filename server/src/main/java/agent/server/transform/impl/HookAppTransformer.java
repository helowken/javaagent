package agent.server.transform.impl;

import agent.hook.utils.App;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.utils.AgentClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class HookAppTransformer extends AbstractTransformer {
    private final Class<?> runnerClass;
    private final String classNamePath;

    public HookAppTransformer(Class<?> runnerClass) {
        this.runnerClass = runnerClass;
        classNamePath = TransformerInfo.getClassNamePath(runnerClass);
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return classNamePath.equals(namePath) && runnerClass.getClassLoader().equals(loader);
    }

    @Override
    public Set<Class<?>> getRefClassSet() {
        Set<Class<?>> classSet = new HashSet<>(super.getRefClassSet());
        classSet.add(App.class);
        return classSet;
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        CtClass ctClass = AgentClassPool.getInstance().get(runnerClass.getName());
        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);
        constructor.insertAfter(App.class.getName() + ".instance = this;");
        return ctClass.toBytecode();
    }

}
