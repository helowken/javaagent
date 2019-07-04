package agent.server.transform;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.ResetClassEvent;
import agent.server.transform.config.ConfigParser;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.impl.*;
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

    public List<TransformResult> transformByConfig(byte[] bs) throws Exception {
        List<TransformContext> transformContextList = new ArrayList<>();
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(bs);
        rsMap.forEach((moduleConfig, configToInfo) -> {
                    Set<Class<?>> classSet = new HashSet<>();
                    List<ErrorTraceTransformer> transformerList = new ArrayList<>();
                    configToInfo.forEach((transformConfig, transformerInfo) -> {
                        classSet.addAll(transformerInfo.getTargetClassSet());
                        for (TransformerConfig transformerConfig : transformConfig.getTransformerConfigList()) {
                            ConfigTransformer transformer = newTransformer(transformerConfig);
                            transformer.setTransformerInfo(transformerInfo);
                            transformer.setConfig(transformerConfig.getConfig());
                            transformerList.add(transformer);
                        }
                    });
                    transformContextList.add(new TransformContext(moduleConfig.getContextPath(), classSet, transformerList, false));
                }
        );
        return transform(transformContextList);
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

    public TransformResult transform(TransformContext transformContext) {
        return transform(Collections.singletonList(transformContext)).get(0);
    }

    public List<TransformResult> transform(List<TransformContext> transformContextList) {
        return transformLock.syncValue(lock -> {
            List<TransformResult> rsList = new ArrayList<>();
            transformContextList.forEach(transformContext -> {
                if (!transformContext.skipRecordClass)
                    contextToTransformedClassSet.put(transformContext.context, transformContext.classSet);
                Set<Class<?>> refClassSet = new HashSet<>(transformContext.classSet);
                transformContext.transformerList.forEach(transformer -> {
                    refClassSet.addAll(transformer.getRefClassSet());
                    instrumentation.addTransformer(transformer, true);
                });
                Exception err;
                try {
                    err = ClassPoolUtils.exec(refClassSet, cp -> {
                        instrumentation.retransformClasses(transformContext.classSet.toArray(new Class[0]));
                        return null;
                    });
                } catch (Exception e) {
                    err = e;
                } finally {
                    transformContext.transformerList.forEach(instrumentation::removeTransformer);
                }
                rsList.add(new TransformResult(transformContext, err));
            });
            return rsList;
        });
    }

    private List<TransformContext> newResetContexts(String contextExpr, Set<String> classExprSet) {
        logger.debug("Reset context expr: {}, class expr set: {}", contextExpr, classExprSet);
        Pattern contextPattern = contextExpr == null ? null : Pattern.compile(contextExpr);
        List<Pattern> classPatterns = classExprSet == null || classExprSet.isEmpty() ?
                null :
                classExprSet.stream()
                        .map(Pattern::compile)
                        .collect(Collectors.toList());
        List<TransformContext> transformContextList = new ArrayList<>();
        classLock.sync(lock ->
                contextToTransformedClassSet.forEach((context, classSet) -> {
                    if (contextPattern == null || contextPattern.matcher(context).matches()) {
                        Set<Class<?>> resetClassSet = new HashSet<>();
                        classSet.forEach(clazz -> {
                            if (classPatterns == null ||
                                    classPatterns.stream().anyMatch(classPattern -> classPattern.matcher(clazz.getName()).matches())) {
                                logger.debug("Add to reset, context: {}, class: {}", context, clazz);
                                resetClassSet.add(clazz);
                            }
                        });
                        transformContextList.add(
                                new TransformContext(context,
                                        resetClassSet,
                                        Collections.singletonList(
                                                new ResetClassTransformer(resetClassSet)
                                        ),
                                        true
                                )
                        );
                    }
                })
        );
        return transformContextList;
    }

    private void cleanAfterReset(List<TransformResult> rsList) {
        classLock.sync(lock ->
                rsList.forEach(transformResult -> {
                    if (transformResult.isSuccess()) {
                        TransformContext transformContext = transformResult.transformContext;
                        final String context = transformContext.context;
                        Set<Class<?>> transformedClassSet = Optional.ofNullable(contextToTransformedClassSet.get(context))
                                .orElseThrow(() -> new RuntimeException("No context found: " + context));
                        transformedClassSet.removeAll(transformContext.classSet);
                        if (transformedClassSet.isEmpty()) {
                            logger.debug("No transformed class left, remove context: {}", context);
                            contextToTransformedClassSet.remove(context);
                        }
                        EventListenerMgr.fireEvent(new ResetClassEvent(transformContext, contextToTransformedClassSet.isEmpty()));
                    }
                })
        );
    }

    public List<TransformResult> resetClasses(String contextExpr, Set<String> classExprSet) {
        List<TransformContext> transformContextList = newResetContexts(contextExpr, classExprSet);
        if (transformContextList.isEmpty()) {
            logger.debug("No class need to reset.");
            return Collections.emptyList();
        } else {
            List<TransformResult> rsList = transform(transformContextList);
            cleanAfterReset(rsList);
            return rsList;
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
