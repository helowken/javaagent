package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.InvokeFilterConfig;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    static {
        Utils.wrapToRtError(() -> {
            SYNTHETIC = ReflectionUtils.getStaticFieldValue(Modifier.class, "SYNTHETIC");
            BRIDGE = ReflectionUtils.getStaticFieldValue(Modifier.class, "BRIDGE");
        });
    }

    public static InvokeFinder getInstance() {
        return instance;
    }

    private InvokeFinder() {
    }

    public InvokeSearchResult find(TargetClassConfig targetClassConfig) {
        ClassConfig classConfig = targetClassConfig.classConfig;
        Class<?> clazz = targetClassConfig.targetClass;
        logger.debug("Start to find invokes for class: {}", clazz.getName());
        InvokeFilterConfig invokeFilterConfig = classConfig.getInvokeFilter();
        if (invokeFilterConfig == null)
            invokeFilterConfig = new InvokeFilterConfig();
        Collection<DestInvoke> rsList = doFind(invokeFilterConfig, clazz);
        logger.debug("===============");
        logger.debug("Invoke filter config: {}", invokeFilterConfig);
        logger.debug("Matched invokes:");
        rsList.forEach(invoke -> logger.debug(invoke.toString()));
        logger.debug("===============");
        return new InvokeSearchResult(clazz, rsList);
    }

    private Collection<DestInvoke> getDestInvokes(Object[] vs) {
        return vs == null ?
                Collections.emptyList() :
                Stream.of(vs)
                        .map(
                                v -> {
                                    if (v instanceof Constructor)
                                        return new ConstructorInvoke((Constructor) v);
                                    else if (v instanceof Method)
                                        return new MethodInvoke((Method) v);
                                    throw new RuntimeException("Invalid entity: " + v);
                                }
                        )
                        .collect(
                                Collectors.toList()
                        );
    }

    private Collection<DestInvoke> doFind(InvokeFilterConfig invokeFilterConfig, Class<?> clazz) {
        Set<DestInvoke> candidateList = new HashSet<>();
        filterOutMeaningless(
                candidateList,
                getDestInvokes(
                        clazz.getDeclaredMethods()
                )
        );
        candidateList.addAll(
                getDestInvokes(
                        clazz.getDeclaredConstructors()
                )
        );
        return filterByCondition(
                invokeFilterConfig.getIncludes(),
                filterByCondition(
                        invokeFilterConfig.getExcludes(),
                        candidateList,
                        false),
                true
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

    private void filterOutMeaningless(Collection<DestInvoke> candidateList, Collection<DestInvoke> invokes) {
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
}
