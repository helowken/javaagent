package agent.server.transform.impl.utils;

import agent.base.utils.Logger;
import agent.base.utils.MethodSignatureUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.TargetClassConfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodFinder {
    private static final Logger logger = Logger.getLogger(MethodFinder.class);
    private static final MethodFinder instance = new MethodFinder();
    private static int SYNTHETIC;
    private static int BRIDGE;

    static {
        Utils.wrapToRtError(() -> {
            SYNTHETIC = ReflectionUtils.getStaticFieldValue(Modifier.class, "SYNTHETIC");
            BRIDGE = ReflectionUtils.getStaticFieldValue(Modifier.class, "BRIDGE");
        });
    }

    public static MethodFinder getInstance() {
        return instance;
    }

    private MethodFinder() {
    }

    public MethodSearchResult find(TargetClassConfig targetClassConfig) {
        ClassConfig classConfig = targetClassConfig.classConfig;
        Class<?> clazz = targetClassConfig.targetClass;
        logger.debug("Start to find methods for class: {}", clazz.getName());
        MethodFilterConfig methodFilterConfig = classConfig.getMethodFilterConfig();
        if (methodFilterConfig == null)
            methodFilterConfig = new MethodFilterConfig();
        Collection<Method> rsList = findByMethodFilter(methodFilterConfig, clazz);
        logger.debug("===============");
        logger.debug("Method filter config: {}", methodFilterConfig);
        logger.debug("Matched methods:");
        rsList.forEach(method -> logger.debug(MethodSignatureUtils.getLongName(method)));
        logger.debug("===============");
        return new MethodSearchResult(clazz, rsList);
    }

    public void consume(TargetClassConfig targetClassConfig, Consumer<MethodSearchResult> resultConsumer) {
        Utils.wrapToRtError(
                () -> resultConsumer.accept(
                        find(targetClassConfig)
                ),
                () -> "Find method list failed."
        );
    }

    private Collection<Method> findByMethodFilter(MethodFilterConfig methodFilterConfig, Class<?> clazz) {
        Set<Method> candidateList = new HashSet<>();
        filterOutMeaningless(candidateList, clazz.getDeclaredMethods());
        return filterByCondition(
                methodFilterConfig.getIncludeExprSet(),
                filterByCondition(
                        methodFilterConfig.getExcludeExprSet(),
                        candidateList,
                        false),
                true
        );
    }

    private Collection<Method> filterByCondition(Set<String> exprSet, Collection<Method> candidateList, final boolean match) {
        Set<String> tmp = exprSet;
        if (tmp == null || tmp.isEmpty())
            tmp = null;
        return Optional.ofNullable(tmp)
                .map(this::compileExprSet)
                .<Collection<Method>>map(
                        patternSet -> candidateList.stream()
                                .filter(method -> match == isMatch(patternSet, method))
                                .collect(Collectors.toList())
                )
                .orElse(candidateList);
    }

    private Pattern compilePattern(String srcPattern) {
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

    private Set<Pattern> compileExprSet(Set<String> exprSet) {
        return exprSet.stream()
                .map(this::compilePattern)
                .collect(Collectors.toSet());
    }

    private boolean isMatch(Set<Pattern> patternSet, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
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

    private void filterOutMeaningless(Collection<Method> candidateList, Method... methods) {
        for (Method method : methods) {
            if (isMethodMeaningful(method.getModifiers()))
                candidateList.add(method);
            else
                logger.debug("Method is meaningless, skip it: {}", MethodSignatureUtils.getLongName(method));
        }
    }

    public static class MethodSearchResult {
        public final Class<?> clazz;
        public final Collection<Method> methods;

        private MethodSearchResult(Class<?> clazz, Collection<Method> methods) {
            this.clazz = clazz;
            this.methods = Collections.unmodifiableCollection(methods);
        }
    }

    public static boolean isMethodMeaningful(int methodModifiers) {
        return (SYNTHETIC & methodModifiers) == 0 &&
                (BRIDGE & methodModifiers) == 0 &&
                !Modifier.isAbstract(methodModifiers);
    }
}
