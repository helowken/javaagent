package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
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
    protected void transformMethod(Method method) throws Exception {
        preTransformMethod(method);
        Set<Method> rsMethods = new HashSet<>();
        getAnnotationClasses().forEach(
                clazz -> collectAllMethods(rsMethods, clazz)
        );

        ProxyRegInfo regInfo = new ProxyRegInfo(method);
        rsMethods.forEach(
                candidateMethod -> maybeReg(method, regInfo, candidateMethod)
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

    protected void preTransformMethod(Method method) {
    }

    private void maybeReg(Method srcMethod, ProxyRegInfo regInfo, Method anntMethod) {
        Class<?> anntClass;
        Annotation[] annotations = anntMethod.getAnnotations();
        if (annotations != null) {
            for (Annotation annt : annotations) {
                anntClass = annt.annotationType();
                if (OnBefore.class.equals(anntClass))
                    regInfo.addBefore(
                            newCallInfo(srcMethod, anntMethod, (OnBefore) annt)
                    );
                else if (OnAfter.class.equals(anntClass))
                    regInfo.addAfter(
                            newCallInfo(srcMethod, anntMethod, (OnAfter) annt)
                    );
                else if (OnReturning.class.equals(anntClass))
                    regInfo.addOnReturning(
                            newCallInfo(srcMethod, anntMethod, (OnReturning) annt)
                    );
                else if (OnThrowing.class.equals(anntClass))
                    regInfo.addOnThrowing(
                            newCallInfo(srcMethod, anntMethod, (OnThrowing) annt)
                    );
            }
        }
    }

    private ProxyCallInfo newCallInfo(Method srcMethod, Method anntMethod, OnBefore annt) {
        return newCallInfo(
                srcMethod,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(Method srcMethod, Method anntMethod, OnAfter annt) {
        return newCallInfo(
                srcMethod,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(Method srcMethod, Method anntMethod, OnReturning annt) {
        return newCallInfo(
                srcMethod,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(Method srcMethod, Method anntMethod, OnThrowing annt) {
        return newCallInfo(
                srcMethod,
                anntMethod,
                annt.mask(),
                annt.argsHint()
        );
    }

    private ProxyCallInfo newCallInfo(Method srcMethod, Method anntMethod, int mask, int argsHint) {
        return new ProxyCallInfo(
                getInstanceOrNull(anntMethod),
                anntMethod,
                mask,
                getOtherArgs(srcMethod, anntMethod, argsHint)
        );
    }

    private Object getInstanceOrNull(Method method) {
        if (Modifier.isStatic(method.getModifiers()))
            return null;
        Object instance = getInstanceForMethod(method);
        if (instance == null)
            throw new RuntimeException("No instance found for method: " + method);
        if (!method.getDeclaringClass().isInstance(instance))
            throw new RuntimeException("Instance type " + instance.getClass().getName() +
                    " is invlid for method declaring class " + method.getDeclaringClass().getName());
        return instance;
    }

    protected abstract Object[] getOtherArgs(Method srcMethod, Method anntMethod, int argsHint);

    protected abstract Object getInstanceForMethod(Method method);

    protected abstract Set<Class<?>> getAnnotationClasses();

}
