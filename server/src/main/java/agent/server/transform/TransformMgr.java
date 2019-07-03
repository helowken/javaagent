package agent.server.transform;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.ResetClassEvent;
import agent.server.transform.config.ConfigParser;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.exception.MultipleTransformException;
import agent.server.transform.impl.MethodFinder;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerClassRegistry;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.system.ResetClassTransformer;

import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransformMgr {
    private static final Logger logger = Logger.getLogger(TransformMgr.class);
    private static final TransformMgr instance = new TransformMgr();
    private Instrumentation instrumentation;
    private Map<String, Set<Class<?>>> contextToTransformedClassSet = new HashMap<>();
    private static final LockObject transformLock = new LockObject();
    private static final LockObject classLock = new LockObject();

    public static TransformMgr getInstance() {
        return instance;
    }

    private TransformMgr() {
    }

    public void init(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void transformByConfig(byte[] bs) throws Exception {
        Map<String, Set<Class<?>>> contextToTargetClassSet = new HashMap<>();
        Map<String, List<ErrorTraceTransformer>> contextToTransformers = new HashMap<>();
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(bs);
        rsMap.forEach((moduleConfig, configToInfo) ->
                configToInfo.forEach((transformConfig, transformerInfo) -> {
                    contextToTargetClassSet.computeIfAbsent(moduleConfig.getContextPath(), key -> new HashSet<>())
                            .addAll(transformerInfo.getTargetClassSet());
                    for (TransformerConfig transformerConfig : transformConfig.getTransformerConfigList()) {
                        ConfigTransformer transformer = newTransformer(transformerConfig);
                        transformer.setTransformerInfo(transformerInfo);
                        transformer.setConfig(transformerConfig.getConfig());
                        contextToTransformers.computeIfAbsent(moduleConfig.getContextPath(), key -> new ArrayList<>()).add(transformer);
                    }
                })
        );
        transform(contextToTargetClassSet, contextToTransformers, false);
    }

    public Map<ModuleConfig, List<MethodFinder.MethodSearchResult>> searchMethods(byte[] bs) throws Exception {
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(bs);
        Map<ModuleConfig, List<MethodFinder.MethodSearchResult>> moduleToSearchResult = new HashMap<>();
        rsMap.forEach((moduleConfig, configToInfo) -> {
                    List<MethodFinder.MethodSearchResult> searchResultList = new ArrayList<>();
                    configToInfo.forEach((transformConfig, transformerInfo) ->
                            transformerInfo.getTargetClassConfigList().forEach(targetClassConfig -> {
                                searchResultList.add(MethodFinder.getInstance().find(targetClassConfig));
                            })
                    );
                    moduleToSearchResult.put(moduleConfig, searchResultList);
                }
        );
        return moduleToSearchResult;
    }

    private Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> parseConfig(byte[] bs) throws Exception {
        logger.debug("Class loader: {}", getClass().getClassLoader());
        logger.debug("Class  {}", getClass().getClassLoader().loadClass("com.fasterxml.jackson.core.type.TypeReference"));
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = new HashMap<>();
        for (ModuleConfig moduleConfig : ConfigParser.parse(bs)) {
            Map<TransformConfig, TransformerInfo> configToInfo = new HashMap<>();
            for (TransformConfig transformConfig : moduleConfig.getTransformConfigList()) {
                List<TargetClassConfig> targetClassConfigList = new ArrayList<>();
                transformConfig.getTargetList().forEach(classConfig -> {
                    Class<?> targetClass = ClassFinder.findClass(moduleConfig.getContextPath(), classConfig.getTargetClass());
                    if (targetClass.isInterface())
                        throw new RuntimeException("Interface class can not be transformed: " + targetClass.getName());
                    logger.debug("{} source location is: {}", targetClass, targetClass.getProtectionDomain().getCodeSource().getLocation());
                    targetClassConfigList.add(new TargetClassConfig(targetClass, classConfig));
                });
                configToInfo.put(transformConfig, new TransformerInfo(targetClassConfigList));
            }
            rsMap.put(moduleConfig, configToInfo);
        }
        return rsMap;
    }

    private ConfigTransformer newTransformer(TransformerConfig transformerConfig) {
        try {
            String className = transformerConfig.getImplClass();
            if (className != null)
                return ConfigTransformer.class.cast(Class.forName(className).newInstance());
            return TransformerClassRegistry.get(transformerConfig.getRef()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Create transformer failed.", e);
        }
    }

    public void transform(String context, Class<?> clazz, ErrorTraceTransformer transformer, boolean skipRecordClass) throws Exception {
        transform(context, Collections.singleton(clazz), Collections.singletonList(transformer), skipRecordClass);
    }

    public void transform(String context, Set<Class<?>> classSet, List<ErrorTraceTransformer> transformerList, boolean skipRecordClass) throws Exception {
        transform(Collections.singletonMap(context, classSet), Collections.singletonMap(context, transformerList), skipRecordClass);
    }

    private void transform(Map<String, Set<Class<?>>> contextToClassSet, Map<String, List<ErrorTraceTransformer>> contextToTransformers, boolean skipRecordClass) throws Exception {
        List<ErrorTraceTransformer> allTransformers = new ArrayList<>();
        contextToTransformers.forEach((context, transformerList) -> allTransformers.addAll(transformerList));
        Exception err = transformLock.syncValue(lock -> {
            try {
                if (!skipRecordClass)
                    contextToTransformedClassSet.putAll(contextToClassSet);
                allTransformers.forEach(transformer -> instrumentation.addTransformer(transformer, true));
                instrumentation.retransformClasses(collectAllClasses(contextToClassSet).toArray(new Class[0]));
                return null;
            } catch (Exception e) {
                return e;
            } finally {
                allTransformers.forEach(instrumentation::removeTransformer);
            }
        });
        Map<String, List<ErrorTraceTransformer>> errorMap = checkTransformResult(contextToTransformers);
        if (!errorMap.isEmpty())
            throw new MultipleTransformException("Transform failed.", err, errorMap);
    }

    private Set<Class<?>> collectAllClasses(Map<String, Set<Class<?>>> contextToClassSet) {
        Set<Class<?>> allClassSet = new HashSet<>();
        contextToClassSet.values().forEach(allClassSet::addAll);
        return allClassSet;
    }

    private Map<String, List<ErrorTraceTransformer>> checkTransformResult(Map<String, List<ErrorTraceTransformer>> contextToTransformers) {
        Map<String, List<ErrorTraceTransformer>> rsMap = new HashMap<>();
        contextToTransformers.forEach((context, transformers) ->
                transformers.forEach(transformer -> {
                    if (transformer.hasError())
                        rsMap.computeIfAbsent(context, key -> new ArrayList<>()).add(transformer);
                })
        );
        return rsMap;
    }

    public void resetClasses(String contextExpr, Set<String> classExprSet) throws Exception {
        logger.debug("Reset context expr: {}, class expr set: {}", contextExpr, classExprSet);
        Pattern contextPattern = contextExpr == null ? null : Pattern.compile(contextExpr);
        List<Pattern> classPatterns = classExprSet == null || classExprSet.isEmpty() ? null : classExprSet.stream().map(Pattern::compile).collect(Collectors.toList());
        Map<String, Set<Class<?>>> rsMap = new HashMap<>();
        classLock.sync(lock ->
                contextToTransformedClassSet.forEach((context, classSet) -> {
                    if (contextPattern == null || contextPattern.matcher(context).matches()) {
                        classSet.forEach(clazz -> {
                            if (classPatterns == null ||
                                    classPatterns.stream().anyMatch(classPattern -> classPattern.matcher(clazz.getName()).matches())) {
                                logger.debug("Add to reset, context: {}, class: {}", context, clazz);
                                rsMap.computeIfAbsent(context, key -> new HashSet<>()).add(clazz);
                            }
                        });
                    }
                })
        );
        if (rsMap.isEmpty()) {
            logger.debug("No class need to reset.");
        } else {
            Set<Class<?>> allClasses = collectAllClasses(rsMap);
            transform(rsMap, Collections.singletonMap("Reset Classes", Collections.singletonList(new ResetClassTransformer(allClasses))), true);
            classLock.sync(lock -> {
                rsMap.forEach((context, classSet) -> {
                    Set<Class<?>> transformedClassSet = Optional.ofNullable(contextToTransformedClassSet.get(context))
                            .orElseThrow(() -> new RuntimeException("No context found: " + context));
                    transformedClassSet.removeAll(classSet);
                    if (transformedClassSet.isEmpty()) {
                        logger.debug("No transformed class left, remove context: {}", context);
                        contextToTransformedClassSet.remove(context);
                    }
                });
                EventListenerMgr.fireEvent(new ResetClassEvent(allClasses, contextToTransformedClassSet.isEmpty()));
            });
        }
    }

    public Map<String, Set<Class<?>>> getContextToTransformedClassSet() {
        return classLock.syncValue(lock -> {
            Map<String, Set<Class<?>>> rsMap = new HashMap<>();
            contextToTransformedClassSet.forEach((context, classSet) ->
                    rsMap.put(context, new HashSet<>(classSet))
            );
            return rsMap;
        });
    }
}
