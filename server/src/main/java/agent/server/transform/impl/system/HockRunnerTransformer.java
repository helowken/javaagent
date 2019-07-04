package agent.server.transform.impl.system;

import agent.hock.utils.JettyRunnerHock;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class HockRunnerTransformer extends AbstractTransformer {
    private final Class<?> runnerClass;
    private final String classNamePath;

    public HockRunnerTransformer(Class<?> runnerClass) {
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
        classSet.add(JettyRunnerHock.class);
        return classSet;
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(runnerClass.getName());
        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);
        constructor.insertAfter(JettyRunnerHock.class.getName() + ".runner = this;");
        byte[] bs = ctClass.toBytecode();
        ctClass.detach();
        return bs;
    }

}
