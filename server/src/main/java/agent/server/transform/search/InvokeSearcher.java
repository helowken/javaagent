package agent.server.transform.search;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.FilterConfig;
import agent.common.config.MethodFilterConfig;
import agent.invoke.ConstructorInvoke;
import agent.invoke.DestInvoke;
import agent.invoke.MethodInvoke;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeFilter;
import agent.server.transform.tools.asm.AsmUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvokeSearcher {
    private static final Logger logger = Logger.getLogger(InvokeSearcher.class);
    private static final InvokeSearcher instance = new InvokeSearcher();
    private static final Map<Class<? extends FilterConfig>, InvokeGetter> invokeGetterMap = new HashMap<>();

    static {
        invokeGetterMap.put(
                MethodFilterConfig.class,
                new MethodGetter()
        );
        invokeGetterMap.put(
                ConstructorFilterConfig.class,
                new ConstructorGetter()
        );
    }

    private InvokeSearcher() {
    }

    public static InvokeSearcher getInstance() {
        return instance;
    }

    public Collection<DestInvoke> search(Class<?> clazz, MethodFilterConfig methodFilterConfig, ConstructorFilterConfig constructorFilterConfig) {
        Set<DestInvoke> result = new HashSet<>();
        if (constructorFilterConfig != null)
            doSearch(constructorFilterConfig, clazz, result);
        else if (methodFilterConfig == null) {
            methodFilterConfig = new MethodFilterConfig();
            methodFilterConfig.setIncludes(
                    Collections.singleton("*")
            );
        }

        if (methodFilterConfig != null)
            doSearch(methodFilterConfig, clazz, result);

        logger.debug("========= Matched invokes:");
        result.forEach(invoke -> logger.debug(invoke.toString()));
        logger.debug("=======================");
        return result;
    }

    private void doSearch(FilterConfig invokeFilterConfig, Class<?> clazz, Set<DestInvoke> invokeSet) {
        InvokeFilter filter = FilterUtils.newInvokeFilter(invokeFilterConfig);
        getInvokeGetter(invokeFilterConfig).get(clazz)
                .stream()
                .filter(
                        invoke -> FilterUtils.isAccept(
                                filter,
                                () -> AsmUtils.getInvokeFullName(
                                        invoke.getName(),
                                        invoke.getDescriptor()
                                )
                        )
                )
                .forEach(invokeSet::add);
    }

    private InvokeGetter getInvokeGetter(FilterConfig filterConfig) {
        return Optional.ofNullable(
                invokeGetterMap.get(
                        filterConfig.getClass()
                )
        ).orElseThrow(
                () -> new RuntimeException("Unsupported filter config type: " + filterConfig.getClass())
        );
    }

    private static boolean isInvokeMeaningful(Method method) {
        int methodModifiers = method.getModifiers();
        return (!ReflectionUtils.isSynthetic(methodModifiers) || ReflectionUtils.isLambda(method)) &&
                !ReflectionUtils.isBridge(methodModifiers) &&
                !Modifier.isAbstract(methodModifiers) &&
                !Modifier.isNative(methodModifiers);
    }

    private interface InvokeGetter {
        Collection<DestInvoke> get(Class<?> clazz);
    }

    private static class MethodGetter implements InvokeGetter {
        @Override
        public Collection<DestInvoke> get(Class<?> clazz) {
            return Stream.of(
                    clazz.getDeclaredMethods()
            )
                    .filter(InvokeSearcher::isInvokeMeaningful)
                    .map(MethodInvoke::new)
                    .collect(
                            Collectors.toList()
                    );
        }
    }

    private static class ConstructorGetter implements InvokeGetter {
        @Override
        public Collection<DestInvoke> get(Class<?> clazz) {
            return Stream.of(
                    clazz.getDeclaredConstructors()
            ).map(ConstructorInvoke::new)
                    .collect(Collectors.toList());
        }
    }
}
