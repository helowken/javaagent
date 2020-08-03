package agent.spring.aop;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.AopMethodFinder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpringAopInvokeFinder implements AopMethodFinder {
    private static final Logger logger = Logger.getLogger(SpringAopInvokeFinder.class);
    private final Map<ClassLoader, Map<Method, Collection<Method>>> loaderToTargetToAopMethods = new ConcurrentHashMap<>();

    @Override
    public Collection<Method> findMethods(Method targetMethod, ClassLoader classLoader) {
        if (classLoader == null)
            return Collections.emptyList();
        Map<Method, Collection<Method>> targetToAopMethods = getTargetToAopMethodsByLoader(classLoader);
        Collection<Method> aopMethods = targetToAopMethods.get(targetMethod);
        return aopMethods == null ? Collections.emptyList() : aopMethods;
    }

    private synchronized Map<Method, Collection<Method>> getTargetToAopMethodsByLoader(ClassLoader classLoader) {
        return loaderToTargetToAopMethods.computeIfAbsent(
                classLoader,
                loader -> {
                    try {
                        return filterEmpty(
                                findTargetToAopMethods(
                                        loadProxyFactoryClass(loader)
                                )
                        );
                    } catch (Exception e) {
                        logger.error("SpringAopFinder init failed.", e);
                        return Collections.emptyMap();
                    }
                }
        );
    }

    private Map<Method, Collection<Method>> filterEmpty(Map<Method, Set<Method>> rsMap) {
        Map<Method, Collection<Method>> targetToAopMethods = new ConcurrentHashMap<>();
        rsMap.forEach(
                (sourceMethod, aopMethods) -> {
                    if (!aopMethods.isEmpty()) {
                        targetToAopMethods.put(
                                sourceMethod,
                                Collections.unmodifiableCollection(aopMethods)
                        );
                        logger.debug("======================");
                        logger.debug("Source Method: {}: ", sourceMethod);
                        aopMethods.forEach(
                                aopMethod -> logger.debug("AOP Method: {}", aopMethod)
                        );
                        logger.debug("======================");
                    }
                }
        );
        return targetToAopMethods;
    }

    private Map<Method, Set<Method>> findTargetToAopMethods(Class<?> proxyFactoryClass) {
        Map<Method, Set<Method>> rsMap = new ConcurrentHashMap<>();
        getProxyFactoryList(proxyFactoryClass)
                .stream()
                .map(this::createAopMethodsMap)
                .forEach(
                        aopMethodsMap -> aopMethodsMap.forEach(
                                (sourceMethod, aopMethods) -> {
                                    rsMap.computeIfAbsent(
                                            sourceMethod,
                                            key -> new HashSet<>()
                                    ).addAll(aopMethods);
                                }
                        )
                );
        return rsMap;
    }

    private Class<?> loadProxyFactoryClass(ClassLoader loader) throws Exception {
        return loader.loadClass("org.springframework.aop.framework.ProxyFactory");
    }

    private List<?> getProxyFactoryList(Class<?> proxyFactoryClass) {
        return JvmtiUtils.getInstance().findObjectsByClass(proxyFactoryClass, Integer.MAX_VALUE);
    }

    @SuppressWarnings("unchecked")
    private Map<Method, Collection<Method>> createAopMethodsMap(Object proxyFactory) {
        try {
            Map<Object, Object> cache = ReflectionUtils.getFieldValue(
                    "methodCache",
                    proxyFactory
            );
            Map<Method, Collection<Method>> targetToAopMethods = new HashMap<>();
            for (Map.Entry entry : cache.entrySet()) {
                Method sourceMethod = ReflectionUtils.getFieldValue("method", entry.getKey());
                List<Object> callChain = (List<Object>) entry.getValue();
                targetToAopMethods.put(
                        sourceMethod,
                        findAopMethods(callChain)
                );
            }
            return Collections.unmodifiableMap(targetToAopMethods);
        } catch (Exception e) {
            logger.error("getMethodCache failed.", e);
            return Collections.emptyMap();
        }
    }

    private Collection<Method> findAopMethods(List<Object> chain) {
        Set<Method> aopMethods = new HashSet<>();
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
        return aopMethods;
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
