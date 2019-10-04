package agent.bytecode.spring.aop;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.BytecodeMethodFinder;
import agent.server.transform.impl.dynamic.MethodInfo;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class SpringAopBytecodeMethodFinder implements BytecodeMethodFinder {
    private static final Logger logger = Logger.getLogger(SpringAopBytecodeMethodFinder.class);

    @Override
    public Set<Method> findBytecodeMethods(MethodInfo targetMethodInfo, Set<Class<?>> hintClassSet, Function<MethodInfo, Method> methodGetter) {
        if (hintClassSet.isEmpty())
            return Collections.emptySet();
        Method targetMethod = methodGetter.apply(targetMethodInfo);
        Set<Method> rsSet = new HashSet<>();
        doFind(targetMethod, rsSet);
        return rsSet;
    }

    private void doFind(Method targetMethod, Set<Method> bytecodeMethodSet) {
        try {
            List<Object> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClassName(
                    "org.springframework.aop.framework.ProxyFactory",
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
            if (methodCache.isEmpty()) {
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
            } else {
                for (Map.Entry entry : methodCache.entrySet()) {
                    Method method = ReflectionUtils.getFieldValue("method", entry.getKey());
                    if (method.equals(targetMethod))
                        return (List<Object>) entry.getValue();
                }
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
