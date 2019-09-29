package test.aop;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.jvmti.JvmtiUtils;
import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static test.utils.ServerTestUtils.initSystemConfig;

public class AAATest {

    @Test
    public void test() throws Exception {
        initSystemConfig();
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AAA aaa = (AAA) context.getBean("aaa");

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


//        doTest();

        aaa.testAAA();
//        Thread.sleep(1000000);
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
