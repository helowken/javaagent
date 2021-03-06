package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.invoke.DestInvoke;
import agent.invoke.proxy.ProxyCallInfo;
import agent.invoke.proxy.ProxyRegInfo;
import agent.server.transform.AnnotationConfigTransformer;
import agent.server.transform.tools.asm.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAnnotationConfigTransformer extends AbstractConfigTransformer implements AnnotationConfigTransformer {

    @Override
    protected void transformDestInvoke(DestInvoke destInvoke) throws Exception {
        ProxyRegInfo regInfo = new ProxyRegInfo(destInvoke);
        getAnntClassToMethods().forEach(
                (anntClass, methods) -> methods.forEach(
                        candidateMethod -> maybeReg(destInvoke, regInfo, anntClass, candidateMethod)
                )
        );
        if (!regInfo.isEmpty())
            addRegInfo(regInfo);
    }

    protected Map<Class<?>, Collection<Method>> getAnntClassToMethods() {
        return getAnnotationClasses()
                .stream()
                .collect(
                        Collectors.toMap(
                                clazz -> clazz,
                                clazz -> {
                                    Set<Method> rsMethods = new HashSet<>();
                                    collectAllMethods(rsMethods, clazz);
                                    return rsMethods;
                                }
                        )
                );
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
                !ReflectionUtils.isJavaIntrinsicPackage(superclass.getName()))
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

    private void maybeReg(DestInvoke destInvoke, ProxyRegInfo regInfo, Class<?> anntClass, Method anntMethod) {
        Class<?> anntType;
        Annotation[] annotations = anntMethod.getAnnotations();
        if (annotations != null) {
            for (Annotation annt : annotations) {
                anntType = annt.annotationType();
                if (OnBefore.class.equals(anntType))
                    regInfo.addBefore(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnBefore) annt, OnBefore::mask, OnBefore::argsHint)
                    );
                else if (OnAfter.class.equals(anntType))
                    regInfo.addAfter(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnAfter) annt, OnAfter::mask, OnAfter::argsHint)
                    );
                else if (OnReturning.class.equals(anntType))
                    regInfo.addOnReturning(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnReturning) annt, OnReturning::mask, OnReturning::argsHint)
                    );
                else if (OnThrowingNotCatch.class.equals(anntType))
                    regInfo.addOnThrowingNotCatch(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnThrowingNotCatch) annt, OnThrowingNotCatch::mask, OnThrowingNotCatch::argsHint)
                    );
                else if (OnThrowing.class.equals(anntType))
                    regInfo.addOnThrowing(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnThrowing) annt, OnThrowing::mask, OnThrowing::argsHint)
                    );
                else if (OnCatching.class.equals(anntType))
                    regInfo.addOnCatching(
                            newCallInfo(destInvoke, anntClass, anntMethod, (OnCatching) annt, OnCatching::mask, OnCatching::argsHint)
                    );
            }
        }
    }

    private <T> ProxyCallInfo newCallInfo(DestInvoke destInvoke, Class<?> anntClass, Method anntMethod,
                                          T obj, Function<T, Integer> maskFunc, Function<T, Integer> argsHintFunc) {
        return new ProxyCallInfo(
                getInstanceOrNull(anntClass, anntMethod),
                anntMethod,
                maskFunc.apply(obj),
                getOtherArgs(
                        destInvoke,
                        anntMethod,
                        argsHintFunc.apply(obj)
                ),
                getTid()
        );
    }

    private Object getInstanceOrNull(Class<?> anntClass, Method anntMethod) {
        if (Modifier.isStatic(anntMethod.getModifiers()))
            return null;
        Object instance = getInstanceForAnntMethod(anntClass, anntMethod);
        if (instance == null)
            throw new RuntimeException("No instance found for method: " + anntMethod);
        if (!anntMethod.getDeclaringClass().isInstance(instance))
            throw new RuntimeException("Instance type " + instance.getClass().getName() +
                    " is invlid for method declaring class " + anntMethod.getDeclaringClass().getName());
        return instance;
    }

    protected abstract Object[] getOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);

    protected abstract Object getInstanceForAnntMethod(Class<?> anntClass, Method anntMethod);

    protected abstract Collection<Class<?>> getAnnotationClasses();
}
