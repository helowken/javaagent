package agent.server.transform.search;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.ConstructorFilterConfig;
import agent.server.transform.config.FilterConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvokeSearcher {
    private static final Logger logger = Logger.getLogger(InvokeSearcher.class);
    private static final InvokeSearcher instance = new InvokeSearcher();
    private static int SYNTHETIC;
    private static int BRIDGE;
    private static final Map<Class<? extends FilterConfig>, InvokeGetter> invokeGetterMap = new HashMap<>();

    static {
        Utils.wrapToRtError(() -> {
            SYNTHETIC = ReflectionUtils.getStaticFieldValue(Modifier.class, "SYNTHETIC");
            BRIDGE = ReflectionUtils.getStaticFieldValue(Modifier.class, "BRIDGE");
        });

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
        else if (methodFilterConfig == null)
            methodFilterConfig = new MethodFilterConfig();

        if (methodFilterConfig != null)
            doSearch(methodFilterConfig, clazz, result);

        logger.debug("========= Matched invokes:");
        result.forEach(invoke -> logger.debug(invoke.toString()));
        logger.debug("=======================");
        return result;
    }

    private void doSearch(FilterConfig invokeFilterConfig, Class<?> clazz, Set<DestInvoke> invokeSet) {
        AgentFilter<DestInvoke> filter = FilterUtils.newInvokeFilter(invokeFilterConfig);
        getInvokeGetter(invokeFilterConfig).get(clazz)
                .stream()
                .filter(
                        invoke -> FilterUtils.isAccept(filter, invoke)
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

    public static class InvokeSearchResult {
        public final Collection<DestInvoke> invokes;

        private InvokeSearchResult(Collection<DestInvoke> invokes) {
            this.invokes = Collections.unmodifiableCollection(invokes);
        }
    }

    private static boolean isInvokeMeaningful(int methodModifiers) {
        return (SYNTHETIC & methodModifiers) == 0 &&
                (BRIDGE & methodModifiers) == 0 &&
                !Modifier.isAbstract(methodModifiers);
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
                    .filter(
                            method -> isInvokeMeaningful(
                                    method.getModifiers()
                            )
                    )
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
