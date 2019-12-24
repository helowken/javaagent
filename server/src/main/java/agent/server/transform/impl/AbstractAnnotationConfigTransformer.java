package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAnnotationConfigTransformer extends AbstractConfigTransformer {

    @Override
    protected void transformDestInvoke(DestInvoke destInvoke) throws Exception {
        Set<Method> rsMethods = new HashSet<>();
        getAnnotationClasses().forEach(
                clazz -> collectAllMethods(rsMethods, clazz)
        );

        ProxyRegInfo regInfo = new ProxyRegInfo(destInvoke);
        rsMethods.forEach(
                candidateMethod -> maybeReg(destInvoke, regInfo, candidateMethod)
        );
        if (!regInfo.isEmpty())
            addRegInfo(regInfo);
    }

    private void collectAllMethods(Set<Method> rsMethods, Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        if (declaredMethods != null) {
            for (Method method : declaredMethods) {
                if (isMethodValid(method)) {
                    rsMethods.add(method);
                }
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null &&
                !ReflectionUtils.isJavaNativePackage(superclass.getName()))
            collectAllMethods(rsMethods, superclass);
    }

    private boolean isMethodValid(Method method) {
        int modifiers = method.getModifiers();
        Annotation[] annotations = method.getAnnotations();
        return !(
                Modifier.isNative(modifiers) ||
                        Modifier.isAbstract(modifiers) ||
                        method.isSynthetic() ||
                        method.isBridge() ||
                        annotations == null ||
                        annotations.length == 0
        );
    }

    private void maybeReg(DestInvoke destInvoke, ProxyRegInfo regInfo, Method anntMethod) {
        Class<?> anntClass;
        Annotation[] annotations = anntMethod.getAnnotations();
        if (annotations != null) {
            for (Annotation annt : annotations) {
                anntClass = annt.annotationType();
                if (OnBefore.class.equals(anntClass))
                    regInfo.addBefore(
                            newCallInfo(destInvoke, anntMethod, (OnBefore) annt)
                    );
                else if (OnAfter.class.equals(anntClass))
                    regInfo.addAfter(
                            newCallInfo(destInvoke, anntMethod, (OnAfter) annt)
                    );
                else if (OnReturning.class.equals(anntClass))
                    regInfo.addOnReturning(
                            newCallInfo(destInvoke, anntMethod, (OnReturning) annt)
                    );
                else if (OnThrowing.class.equals(anntClass))
                    regInfo.addOnThrowing(
                            newCallInfo(destInvoke, anntMethod, (OnThrowing) annt)
                    );
            }
        }
    }

    private ProxyCallInfo newCallInfo(DestInvoke destInvoke, Method anntMethod, OnBefore annt) {
        return newCallInfo(
                destInvoke,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(DestInvoke destInvoke, Method anntMethod, OnAfter annt) {
        return newCallInfo(
                destInvoke,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(DestInvoke destInvoke, Method anntMethod, OnReturning annt) {
        return newCallInfo(
                destInvoke,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(DestInvoke destInvoke, Method anntMethod, OnThrowing annt) {
        return newCallInfo(
                destInvoke,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(DestInvoke destInvoke, Method anntMethod, int mask, int argsHint) {
        return new ProxyCallInfo(
                getInstanceOrNull(anntMethod),
                anntMethod,
                mask,
                getOtherArgs(destInvoke, anntMethod, argsHint)
        );
    }

    private Object getInstanceOrNull(Method anntMethod) {
        if (Modifier.isStatic(anntMethod.getModifiers()))
            return null;
        Object instance = getInstanceForMethod(anntMethod);
        if (instance == null)
            throw new RuntimeException("No instance found for method: " + anntMethod);
        if (!anntMethod.getDeclaringClass().isInstance(instance))
            throw new RuntimeException("Instance type " + instance.getClass().getName() +
                    " is invlid for method declaring class " + anntMethod.getDeclaringClass().getName());
        return instance;
    }

    protected abstract Object[] getOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);

    protected abstract Object getInstanceForMethod(Method method);

    protected abstract Set<Class<?>> getAnnotationClasses();

}
