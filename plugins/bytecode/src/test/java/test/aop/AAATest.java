package test.aop;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.bytecode.spring.aop.SpringAopBytecodeMethodFinder;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.impl.dynamic.MethodInfo;
import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.framework.AdvisorChainFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AAATest {
    private static final JvmtiUtils jvmtiUtils = JvmtiUtils.getInstance();

    static {
        jvmtiUtils.load(System.getProperty("user.dir") + "/../../packaging/resources/server/native/libagent_jvmti_JvmtiUtils.so");
    }

    @Test
    public void test() throws Exception {
        SystemConfig.load(new File(System.getProperty("usr.dir"), "../../packaging/resources/server/conf/server.conf").getAbsolutePath());

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AAA aaa = (AAA) context.getBean("aaa");


//        doTest2(aaa);
//        doTest();
//        doTest3("testAAA");
//        doTest3("testBBB");
        doTest4("testAAA");
        doTest4("testBBB");
        aaa.testAAA();
        System.out.println("-------------------");
        aaa.testBBB();

//        Thread.sleep(1000000);
    }

    private void doTest4(String methodName) {
        MethodInfo methodInfo = new MethodInfo(
                AAA.class.getName(),
                methodName,
                "()V",
                1
        );
        new SpringAopBytecodeMethodFinder().findBytecodeMethods(
                methodInfo,
                Collections.singleton(Object.class),
                targetMethodInfo -> Utils.wrapToRtError(() ->
                        ReflectionUtils.findFirstMethod(
                                targetMethodInfo.className,
                                targetMethodInfo.methodName,
                                targetMethodInfo.signature
                        )
                )
        ).forEach(System.out::println);
    }

    private void doTest3(String methodName) throws Exception {
        System.out.println("=============: " + methodName);
        Method aaaMethod = ReflectionUtils.findFirstMethod(AAA.class, methodName);
        List<ProxyFactory> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClass(ProxyFactory.class, Integer.MAX_VALUE);
        for (ProxyFactory proxyFactory : proxyFactoryList) {
            Map<Method, List<Object>> methodCache = ReflectionUtils.getFieldValue("methodCache", proxyFactory);
            List<Object> chain = null;
            if (methodCache.isEmpty()) {
                AdvisorChainFactory advisorChainFactory = ReflectionUtils.invoke("getAdvisorChainFactory", proxyFactory);
                chain = advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(proxyFactory, aaaMethod, null);
            } else {
                for (Map.Entry entry : methodCache.entrySet()) {
                    Method method = ReflectionUtils.getFieldValue("method", entry.getKey());
                    if (method.equals(aaaMethod)) {
                        chain = (List<Object>) entry.getValue();
                        break;
                    }
                }
                if (chain == null)
                    throw new RuntimeException("No chain found for method: " + aaaMethod);
            }
            List<Method> bytecodeMethods = new ArrayList<>();
            for (Object element : chain) {
                Method aopMethod = findBytecodeMethod(element);
                if (aopMethod != null)
                    bytecodeMethods.add(aopMethod);
                else {
                    try {
                        Object advice = ReflectionUtils.getFieldValue("advice", element);
                        aopMethod = findBytecodeMethod(advice);
                        if (aopMethod != null)
                            bytecodeMethods.add(aopMethod);
                    } catch (Exception e) {
                    }
                }
            }
            bytecodeMethods.forEach(System.out::println);
        }
        System.out.println();
    }

    private Method findBytecodeMethod(Object element) {
        return element instanceof AbstractAspectJAdvice ?
                ((AbstractAspectJAdvice) element).getAspectJAdviceMethod() :
                null;
    }

    private void doTest2(AAA aaa) throws Exception {
        Function<Field, Boolean> filterFunc = field -> !Modifier.isStatic(field.getModifiers());
        Map<Field, Object> fieldValueMap = getFieldValueMap(aaa, filterFunc);
        Set<ProxyFactory> proxyFactorySet = new HashSet<>();
        fieldValueMap.forEach((field, value) -> {
            Map<Field, Object> subFieldValueMap = getFieldValueMap(value, filterFunc);
            subFieldValueMap.values()
                    .stream()
                    .filter(ProxyFactory.class::isInstance)
                    .forEach(proxyFactory -> proxyFactorySet.add((ProxyFactory) proxyFactory));
        });
        proxyFactorySet.forEach(this::printMethods);
    }

    private Map<Field, Object> getFieldValueMap(Object target, Function<Field, Boolean> filter) {
        Set<Field> fieldSet = new HashSet<>(
                Arrays.asList(
                        target.getClass().getDeclaredFields()
                )
        );
        fieldSet.addAll(
                Arrays.asList(
                        target.getClass().getFields()
                )
        );
        return fieldSet.stream()
                .filter(filter::apply)
                .collect(
                        Collectors.toMap(
                                field -> field,
                                field -> Utils.wrapToRtError(
                                        () -> ReflectionUtils.getFieldValue(field.getName(), target)
                                )
                        )
                );
    }

    private void doTest() {
        List<ProxyFactory> proxyFactoryList = JvmtiUtils.getInstance().findObjectsByClass(ProxyFactory.class, Integer.MAX_VALUE);
        proxyFactoryList.forEach(this::printMethods);
    }

    private void printMethods(ProxyFactory proxyFactory) {
        System.out.println(proxyFactory.getTargetClass());
        Advisor[] advisors = proxyFactory.getAdvisors();
        if (advisors != null) {
            Stream.of(advisors)
                    .forEach(advisor -> {
                        Advice advice = advisor.getAdvice();
                        if (advice instanceof AbstractAspectJAdvice) {
                            AbstractAspectJAdvice aspectJAdvice = (AbstractAspectJAdvice) advice;
                            System.out.println(aspectJAdvice.getAspectName() + " => " + aspectJAdvice.getAspectJAdviceMethod());
                        }
                    });
        }
    }
}
