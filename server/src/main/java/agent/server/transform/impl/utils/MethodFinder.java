package agent.server.transform.impl.utils;

import agent.base.utils.ClassUtils;
import agent.base.utils.Logger;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.MethodConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.exception.MultiMethodFoundException;
import agent.server.transform.impl.TargetClassConfig;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodFinder {
    private static final Logger logger = Logger.getLogger(MethodFinder.class);
    private static final MethodFinder instance = new MethodFinder();

    public static MethodFinder getInstance() {
        return instance;
    }

    private MethodFinder() {
    }

    public MethodSearchResult rawFind(AgentClassPool cp, TargetClassConfig targetClassConfig) throws Exception {
        Set<String> methodLongNames = new HashSet<>();
        ClassConfig classConfig = targetClassConfig.classConfig;
        CtClass ctClass = cp.get(classConfig.getTargetClass());
        logger.debug("Start to find methods for class: {}", ctClass.getName());
        List<CtMethod> candidateList = new ArrayList<>();
        if (classConfig.getMethodConfigList() != null)
            candidateList.addAll(findByMethodConfig(classConfig.getMethodConfigList(), ctClass, cp));
        if (classConfig.getMethodFilterConfig() != null)
            candidateList.addAll(findByMethodFilter(classConfig.getMethodFilterConfig(), ctClass));
        List<CtMethod> rsList = collectMethodsIfNeeded(methodLongNames, candidateList);
        logger.debug("===============");
        logger.debug("Matched methods:");
        rsList.forEach(method -> logger.debug(method.getLongName()));
        logger.debug("===============");
        return new MethodSearchResult(ctClass, rsList);
    }

    public void consume(AgentClassPool cp, TargetClassConfig targetClassConfig, Consumer<MethodSearchResult> resultConsumer) {
        try {
            resultConsumer.accept(
                    rawFind(cp, targetClassConfig)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Find method list failed.", e);
        }
    }

    private List<CtMethod> findByMethodConfig(List<MethodConfig> methodConfigList, CtClass ctClass, AgentClassPool cp) throws Exception {
        List<CtMethod> methodList = new ArrayList<>();
        for (MethodConfig methodConfig : methodConfigList) {
            String methodName = methodConfig.getName();
            String[] argTypes = methodConfig.getArgTypes();
            CtMethod method = argTypes == null ?
                    getMethodByName(methodName, ctClass)
                    : ctClass.getDeclaredMethod(methodName, cp.get(argTypes));
            methodList.add(method);
        }
        return methodList;
    }

    private List<CtMethod> collectMethodsIfNeeded(Set<String> methodLongNames, List<CtMethod> methodList) {
        List<CtMethod> rsList = new ArrayList<>();
        for (CtMethod method : methodList) {
            String longName = method.getLongName();
            if (!methodLongNames.contains(longName)) {
                rsList.add(method);
                methodLongNames.add(longName);
            }
        }
        return rsList;
    }

    private List<CtMethod> findByMethodFilter(MethodFilterConfig methodFilterConfig, CtClass ctClass) {
        List<CtMethod> candidateList = new ArrayList<>();
        filterOutNoBody(candidateList, ctClass.getDeclaredMethods());

        if (!methodFilterConfig.isOnlyDeclared()) {
            logger.debug("Search all methods.");
            if (methodFilterConfig.isSkipJavaNative()) {
                logger.debug("Skip java native methods.");
                filterOutJavaNative(candidateList, ctClass.getMethods());
            } else {
                logger.debug("Include java native methods.");
                candidateList.addAll(Arrays.asList(ctClass.getMethods()));
            }
        } else
            logger.debug("Search methods only declared.");

        logger.debug("Filter methods according to include and exclude expr set.");
        return filterByCondition(
                methodFilterConfig.getIncludeExprSet(),
                filterByCondition(
                        methodFilterConfig.getExcludeExprSet(),
                        candidateList,
                        false),
                true
        );
    }

    private List<CtMethod> filterByCondition(Set<String> exprSet, List<CtMethod> candidateList, final boolean match) {
        Set<String> tmp = exprSet;
        if (tmp == null || tmp.isEmpty())
            tmp = null;
        return Optional.ofNullable(tmp)
                .map(this::compileExprSet)
                .map(patternSet ->
                        candidateList.stream()
                                .filter(method -> match == isMatch(patternSet, method))
                                .collect(Collectors.toList())
                )
                .orElse(candidateList);
    }

    private Set<Pattern> compileExprSet(Set<String> exprSet) {
        return exprSet.stream().map(Pattern::compile).collect(Collectors.toSet());
    }

    private boolean isMatch(Set<Pattern> patternSet, CtMethod method) {
        final String fullName = method.getDeclaringClass().getName() + "." + method.getName();
        return patternSet.stream().anyMatch(pattern -> pattern.matcher(fullName).matches());
    }

    private void filterOutNoBody(List<CtMethod> candidateList, CtMethod... methods) {
        for (CtMethod method : methods) {
            if (method.getMethodInfo().getCodeAttribute() != null)
                candidateList.add(method);
            else
                logger.debug("Method has no body, skip it: {}", method.getLongName());
        }
    }

    private void filterOutJavaNative(List<CtMethod> candidateList, CtMethod... methods) {
        for (CtMethod method : methods) {
            String packageName = method.getDeclaringClass().getPackageName();
            boolean isNative = ClassUtils.isJavaNativePackage(packageName);
            if (isNative)
                logger.debug("Method is java native, skip it: {}", method.getLongName());
            else
                candidateList.add(method);
        }
    }

    private CtMethod getMethodByName(String methodName, CtClass ctClass) throws NotFoundException {
        CtMethod foundMethod = null;
        for (CtMethod method : ctClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (foundMethod == null)
                    foundMethod = method;
                else
                    throw new MultiMethodFoundException("More than one methods have same name.");
            }
        }
        if (foundMethod != null)
            return foundMethod;
        return ctClass.getDeclaredMethod(methodName);
    }

    public static class MethodSearchResult {
        public final CtClass ctClass;
        public final List<CtMethod> methodList;

        private MethodSearchResult(CtClass ctClass, List<CtMethod> methodList) {
            this.ctClass = ctClass;
            this.methodList = Collections.unmodifiableList(methodList);
        }
    }
}
