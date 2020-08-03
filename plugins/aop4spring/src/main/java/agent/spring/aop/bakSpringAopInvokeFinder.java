package agent.spring.aop;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.AopMethodFinder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class bakSpringAopInvokeFinder implements AopMethodFinder {
    private static final Logger logger = Logger.getLogger(bakSpringAopInvokeFinder.class);
    private volatile List<AopItem> aopItemList = null;
    private final Map<Method, Set<Method>> targetToAopMethods = new ConcurrentHashMap<>();

    @Override
    public Set<Method> findMethods(Method targetMethod, ClassLoader classLoader) {
        if (!canBeAOP(targetMethod))
            return Collections.emptySet();
        return Collections.unmodifiableSet(
                targetToAopMethods.computeIfAbsent(
                        targetMethod,
                        method -> doFind(targetMethod, classLoader)
                )
        );
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

    private List<AopItem> getAopItemList(ClassLoader classLoader) throws Exception {
        if (aopItemList == null) {
            synchronized (this) {
                if (aopItemList == null) {
                    List<?> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClass(
                            classLoader.loadClass("org.springframework.aop.framework.ProxyFactory"),
                            Integer.MAX_VALUE
                    );
                    aopItemList = proxyFactoryList.stream()
                            .map(AopItem::new)
                            .collect(Collectors.toList());
                }
            }
        }
        return aopItemList;
    }

    private Set<Method> doFind(Method targetMethod, ClassLoader classLoader) {
        Set<Method> rsSet = new HashSet<>();
        try {
            getAopItemList(classLoader)
                    .stream()
                    .map(
                            aopItem -> aopItem.getCallbackChain(targetMethod)
                    )
                    .filter(Objects::nonNull)
                    .forEach(
                            chain -> collectAopMethods(chain, rsSet)
                    );
        } catch (Exception e) {
            logger.error("doFind failed.", e);
        }
        return rsSet;
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
        long st = System.currentTimeMillis();
        final String methodName = "getAspectJAdviceMethod";
        try {
            Method aopMethod = ReflectionUtils.invoke(methodName, element);
            if (aopMethod != null) {
                aopMethods.add(aopMethod);
                return true;
            }
        } catch (Exception e) {
//            logger.error("find aop method failed.", e);
        } finally {
            long et = System.currentTimeMillis();
            logger.error("AopFindAopMethod: {}", (et - st));
        }
        return false;
    }

    private static class AopItem {
        final Object proxyFactory;
        final Map<Object, Object> methodCache;
        final Object advisorChainFactory;
        final Method getAdviceMethod;

        private AopItem(Object proxyFactory) {
            this.proxyFactory = proxyFactory;
            this.methodCache = getMethodCache();
            this.advisorChainFactory = getAdvisorChainFactory();
            this.getAdviceMethod = getGetAdviceMethod();
        }

        private Method getGetAdviceMethod() {
            if (advisorChainFactory != null) {
                try {
                    Method method = ReflectionUtils.findFirstMethod(
                            advisorChainFactory.getClass(),
                            "getInterceptorsAndDynamicInterceptionAdvice"
                    );
                    method.setAccessible(true);
                    return method;
                } catch (Exception e) {
                    logger.error("getGetAdviceMethod failed.", e);
                }
//                return (List<Object>) method.invoke(advisorChainFactory, proxyFactory, targetMethod, null);
            }
            return null;
        }

        private Object getAdvisorChainFactory() {
            try {
                return ReflectionUtils.invoke(
                        "getAdvisorChainFactory",
                        proxyFactory
                );
            } catch (Exception e) {
                logger.error("getAdvisorChainFactory failed.", e);
                return null;
            }
        }

        private Map getMethodCache() {
            try {
                return ReflectionUtils.getFieldValue(
                        "methodCache",
                        proxyFactory
                );
            } catch (Exception e) {
                logger.error("getMethodCache failed.", e);
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        private List<Object> getCallbackChain(Method targetMethod) {
            long st = System.currentTimeMillis();
            try {
                if (methodCache != null) {
                    for (Map.Entry entry : methodCache.entrySet()) {
                        Method method = ReflectionUtils.getFieldValue("method", entry.getKey());
                        if (method.equals(targetMethod))
                            return (List<Object>) entry.getValue();
                    }
                }
                if (getAdviceMethod != null)
                    return (List<Object>) getAdviceMethod.invoke(advisorChainFactory, proxyFactory, targetMethod, null);
            } catch (Exception e) {
                logger.error("get callback chain failed.", e);
            } finally {
                long et = System.currentTimeMillis();
                logger.error("AopGetCallbackChain: {}", (et - st));
            }
            return null;
        }
    }
}
