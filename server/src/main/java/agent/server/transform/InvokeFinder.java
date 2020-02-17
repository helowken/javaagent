package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.ConstructorFilterConfig;
import agent.server.transform.config.FilterConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvokeFinder {
    private static final Logger logger = Logger.getLogger(InvokeFinder.class);
    private static final InvokeFinder instance = new InvokeFinder();
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

    public static InvokeFinder getInstance() {
        return instance;
    }

    private InvokeFinder() {
    }

    public InvokeSearchResult find(Class<?> clazz, Collection<FilterConfig> invokeFilterConfigs) {
        logger.debug("Start to find invokes for class: {}", clazz.getName());
        logger.debug("===============");
        Set<DestInvoke> result = new HashSet<>();
        Collection<FilterConfig> filterConfigs = invokeFilterConfigs;
        if (filterConfigs.isEmpty())
            filterConfigs = Collections.singletonList(
                    new MethodFilterConfig()
            );
        filterConfigs.forEach(
                invokeFilterConfig -> {
                    logger.debug("Invoke filter config: {}", invokeFilterConfig);
                    result.addAll(
                            doFind(invokeFilterConfig, clazz)
                    );
                }
        );
        logger.debug("Matched invokes:");
        result.forEach(invoke -> logger.debug(invoke.toString()));
        logger.debug("===============");
        return new InvokeSearchResult(clazz, result);
    }

    private Collection<DestInvoke> doFind(FilterConfig invokeFilterConfig, Class<?> clazz) {
        return filterByCondition(
                invokeFilterConfig.getIncludes(),
                filterByCondition(
                        invokeFilterConfig.getExcludes(),
                        getInvokeGetter(invokeFilterConfig).get(clazz),
                        false),
                true
        );
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


    private Collection<DestInvoke> filterByCondition(Set<String> exprSet, Collection<DestInvoke> candidateList, final boolean match) {
        Set<String> tmp = exprSet;
        if (tmp == null || tmp.isEmpty())
            tmp = null;
        return Optional.ofNullable(tmp)
                .map(this::compileExprSet)
                .<Collection<DestInvoke>>map(
                        patternSet -> candidateList.stream()
                                .filter(invoke -> match == isMatch(patternSet, invoke))
                                .collect(Collectors.toList())
                )
                .orElse(candidateList);
    }

    private Set<Pattern> compileExprSet(Set<String> exprSet) {
        return exprSet.stream()
                .map(InvokeFinder::compilePattern)
                .collect(Collectors.toSet());
    }

    public static Pattern compilePattern(String srcPattern) {
        String pattern = srcPattern.replaceAll(" ", "");
        if (!pattern.contains("("))
            pattern += "\\(.*\\)";
        else
            pattern = pattern.replace("(", "\\(").replace(")", "\\)");
        final String s = pattern;
        logger.debug("Compile pattern {} to {}", srcPattern, s);
        return Utils.wrapToRtError(
                () -> Pattern.compile(s),
                () -> "Invalid pattern: " + srcPattern + ", converted pattern: " + s
        );
    }

    public static boolean isMatch(Collection<Pattern> patternSet, DestInvoke invoke) {
        StringBuilder sb = new StringBuilder();
        sb.append(invoke.getName()).append("(");
        Class<?>[] paramTypes = invoke.getParamTypes();
        if (paramTypes != null) {
            int count = 0;
            for (Class<?> paramType : paramTypes) {
                if (count > 0)
                    sb.append(",");
                sb.append(paramType.getName());
                ++count;
            }
        }
        sb.append(")");
        String fullName = sb.toString();
        return patternSet.stream().anyMatch(
                pattern -> pattern.matcher(fullName).matches()
        );
    }

    private static void filterOutMeaningless(Collection<DestInvoke> candidateList, Collection<DestInvoke> invokes) {
        for (DestInvoke invoke : invokes) {
            if (isInvokeMeaningful(invoke.getModifiers()))
                candidateList.add(invoke);
            else
                logger.debug("Invoke is meaningless, skip it: {}", invoke);
        }
    }

    public static class InvokeSearchResult {
        public final Class<?> clazz;
        public final Collection<DestInvoke> invokes;

        private InvokeSearchResult(Class<?> clazz, Collection<DestInvoke> invokes) {
            this.clazz = clazz;
            this.invokes = Collections.unmodifiableCollection(invokes);
        }
    }

    public static boolean isInvokeMeaningful(int methodModifiers) {
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
            Set<DestInvoke> candidateList = new HashSet<>();
            filterOutMeaningless(
                    candidateList,
                    Stream.of(
                            clazz.getDeclaredMethods()
                    ).map(MethodInvoke::new)
                            .collect(Collectors.toList())
            );
            return candidateList;
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
