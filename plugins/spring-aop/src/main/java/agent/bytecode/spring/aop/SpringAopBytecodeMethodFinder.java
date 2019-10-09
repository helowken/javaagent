package agent.bytecode.spring.aop;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.BytecodeMethodFinder;
import agent.server.transform.impl.dynamic.MethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class SpringAopBytecodeMethodFinder implements BytecodeMethodFinder {
    private static final Logger logger = Logger.getLogger(SpringAopBytecodeMethodFinder.class);

    @Override
    public Set<Method> findBytecodeMethods(MethodInfo targetMethodInfo, ClassLoader classLoader, Function<MethodInfo, Method> methodGetter) {
        if (!canBeAop(targetMethodInfo)) {
//            logger.debug("Can not be aop: {}", targetMethodInfo);
            return Collections.emptySet();
        }
        Method targetMethod = methodGetter.apply(targetMethodInfo);
        Set<Method> rsSet = new HashSet<>();
        doFind(targetMethod, classLoader, rsSet);
//        if (rsSet.isEmpty())
//            logger.debug("No bytecode methods found for: {}", targetMethodInfo);
//        else
//            logger.debug("Found bytecode methods {} for : {}", rsSet, targetMethodInfo);
        return rsSet;
    }

    private boolean canBeAop(MethodInfo targetMethodInfo) {
        return ReflectionUtils.canBeOverridden(
                targetMethodInfo.classModifiers,
                targetMethodInfo.methodModifiers
        ) &&
                !(Modifier.isAbstract(targetMethodInfo.methodModifiers) ||
                        Modifier.isStatic(targetMethodInfo.classModifiers) ||
                        Modifier.isAbstract(targetMethodInfo.classModifiers)
                );
    }

    private void doFind(Method targetMethod, ClassLoader classLoader, Set<Method> bytecodeMethodSet) {
        try {
            List<?> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClass(
                    classLoader.loadClass("org.springframework.aop.framework.ProxyFactory"),
                    Integer.MAX_VALUE
            );
            proxyFactoryList.stream()
                    .map(proxyFactory -> getCallbackChain(proxyFactory, targetMethod))
                    .filter(Objects::nonNull)
                    .forEach(chain -> collectBytecodeMethods(chain, bytecodeMethodSet));
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
//                    logger.debug("Get from cache: {}", method);
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
//                logger.debug("Get from advisorChainFactory: {}", method);
                return (List<Object>) method.invoke(advisorChainFactory, proxyFactory, targetMethod, null);
            }
        } catch (Exception e) {
            logger.error("get callback chain failed.", e);
        }
        return null;
    }

    private void collectBytecodeMethods(List<Object> chain, Set<Method> bytecodeMethodSet) {
        for (Object element : chain) {
            if (!findBytecodeMethod(element, bytecodeMethodSet)) {
                try {
                    findBytecodeMethod(
                            ReflectionUtils.getFieldValue("advice", element),
                            bytecodeMethodSet
                    );
                } catch (Exception e) {
//                    logger.debug("collect bytecode methods failed, element is: {}, error: \n{}", element, Utils.getErrorStackStrace(e));
                }
            }
        }
    }

    private boolean findBytecodeMethod(Object element, Collection<Method> bytecodeMethods) {
        String methodName = "getAspectJAdviceMethod";
        try {
            if (ReflectionUtils.findFirstMethod(element.getClass(), methodName) != null) {
                Method method = ReflectionUtils.invoke(methodName, element);
                if (method != null) {
                    bytecodeMethods.add(method);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("find bytecode method failed.", e);
        }
        return false;
    }
}
