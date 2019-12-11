package agent.server.transform.tools.asm;

import agent.base.utils.Utils;
import agent.server.transform.impl.AbstractConfigTransformer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AsmTransformer extends AbstractConfigTransformer {

    @Override
    protected void transformMethods(Collection<Method> destMethods) throws Exception {
        newClassToMethods(destMethods)
                .forEach(
                        (clazz, methods) -> Utils.wrapToRtError(
                                () -> {
                                    byte[] classData = getTransformContext().getClassPool().getClassData(clazz);
                                }
                        )
                );
    }

    private Map<Class<?>, Collection<Method>> newClassToMethods(Collection<Method> destMethods) {
        Map<Class<?>, Collection<Method>> classToMethods = new HashMap<>();
        destMethods.forEach(
                method -> classToMethods.computeIfAbsent(
                        method.getDeclaringClass(),
                        key -> new HashSet<>()
                ).add(method)
        );
        return classToMethods;
    }

    @Override
    protected void transformMethod(Method method) throws Exception {

    }

    @Override
    public String getRegKey() {
        return null;
    }
}
