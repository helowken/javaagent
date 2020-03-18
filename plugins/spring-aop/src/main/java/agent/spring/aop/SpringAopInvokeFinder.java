package agent.spring.aop;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.AopMethodFinder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class SpringAopInvokeFinder implements AopMethodFinder {
    private static final Logger logger = Logger.getLogger(SpringAopInvokeFinder.class);

    @Override
    public Set<Method> findMethods(Method targetMethod, ClassLoader classLoader) {
        if (!canBeAOP(targetMethod))
            return Collections.emptySet();

        Set<Method> rsSet = new HashSet<>();
        doFind(targetMethod, classLoader, rsSet);
        return rsSet;
    }

    private boolean canBeAOP(Method targetMethod) {
        int classModifiers = targetMethod.getDeclaringClass().getModifiers();
        int invokeModifiers = targetMethod.getModifiers();
        return ReflectionUtils.canBeOverridden(classModifiers, invokeModifiers) &&
                !(Modifier.isAbstract(invokeModifiers) ||
                        Modifier.isStatic(classModifiers) ||
                        Modifier.isAbstract(classModifiers)
                );
    }

    private void doFind(Method targetMethod, ClassLoader classLoader, Set<Method> aopMethods) {
        try {
            List<?> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClass(
                    classLoader.loadClass("org.springframework.aop.framework.ProxyFactory"),
                    Integer.MAX_VALUE
            );
            proxyFactoryList.stream()
                    .map(proxyFactory -> getCallbackChain(proxyFactory, targetMethod))
                    .filter(Objects::nonNull)
                    .forEach(chain -> collectAopMethods(chain, aopMethods));
        } catch (Exception e) {
            logger.error("doFind failed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> getCallbackChain(Object proxyFactory, Method targetMethod) {
        try {
            Map<Method, List<Object>> methodCache = ReflectionUtils.getFieldValue(
                    "methodCache",
                    proxyFactory
            );
            for (Map.Entry entry : methodCache.entrySet()) {
                Method method = ReflectionUtils.getFieldValue("method", entry.getKey());
                if (method.equals(targetMethod)) {
                    return (List<Object>) entry.getValue();
                }
            }
            Object advisorChainFactory = ReflectionUtils.invoke(
                    "getAdvisorChainFactory",
                    proxyFactory
            );
            if (advisorChainFactory != null) {
                Method method = ReflectionUtils.findFirstMethod(
                        advisorChainFactory.getClass(),
                        "getInterceptorsAndDynamicInterceptionAdvice"
                );
                method.setAccessible(true);
                return (List<Object>) method.invoke(advisorChainFactory, proxyFactory, targetMethod, null);
            }
        } catch (Exception e) {
            logger.error("get callback chain failed.", e);
        }
        return null;
    }

    private void collectAopMethods(List<Object> chain, Set<Method> aopMethods) {
        for (Object element : chain) {
            if (!findAopMethod(element, aopMethods)) {
                try {
                    findAopMethod(
                            ReflectionUtils.getFieldValue("advice", element),
                            aopMethods
                    );
                } catch (Exception e) {
//                    logger.error("collect aop methods failed, element is: {}", e, element);
                }
            }
        }
    }

    private boolean findAopMethod(Object element, Collection<Method> aopMethods) {
        final String methodName = "getAspectJAdviceMethod";
        try {
            Method aopMethod = ReflectionUtils.invoke(methodName, element);
            if (aopMethod != null) {
                aopMethods.add(aopMethod);
                return true;
            }
        } catch (Exception e) {
//            logger.error("find aop method failed.", e);
        }
        return false;
    }
}
