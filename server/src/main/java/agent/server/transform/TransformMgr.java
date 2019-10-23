package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.base.utils.*;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.AppTypePluginFilter;
import agent.server.classloader.DynamicClassLoader;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.ConfigParseFactory;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.UpdateClassDataTransformer;
import agent.server.transform.MethodFinder.MethodSearchResult;
import agent.server.transform.revision.ClassDataRepository;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TransformMgr {
    private static final Logger logger = Logger.getLogger(TransformMgr.class);
    private static final TransformMgr instance = new TransformMgr();
    private Instrumentation instrumentation;
    private Map<String, Set<Class<?>>> contextToTransformedClassSet = new HashMap<>();
    private Map<String, DynamicClassLoader> contextToDynamicClassLoader = new HashMap<>();
    private static final LockObject classLock = new LockObject();
    private static final LockObject loaderLock = new LockObject();

    public static TransformMgr getInstance() {
        return instance;
    }

    private TransformMgr() {
    }

    public void init(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    private DynamicClassLoader getDynamicClassLoader(String context) {
        ClassFinder classFinder = getClassFinder();
        return loaderLock.syncValue(lock ->
                contextToDynamicClassLoader.computeIfAbsent(context,
                        key -> {
                            final ClassLoader classLoader = classFinder.findClassLoader(context);
                            try {
                                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(
                                        ReflectionUtils.getFieldValue("parent", classLoader)
                                );
                                classFinder.setParentClassLoader(context, dynamicClassLoader);
                                return dynamicClassLoader;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                logger.debug("context: {}", context);
                                ClassLoaderUtils.printClassLoaderCascade(classLoader);
                            }
                        }
                )
        );
    }

    public List<TransformResult> transformByConfig(ConfigItem configItem) {
        List<TransformContext> transformContextList = new ArrayList<>();
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(configItem);
        rsMap.forEach((moduleConfig, configToInfo) -> {
                    Set<Class<?>> classSet = new HashSet<>();
                    List<AgentTransformer> transformerList = new ArrayList<>();
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

    public void searchMethods(ConfigItem item, SearchFunc func) {
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(item);
        rsMap.forEach((moduleConfig, configToInfo) ->
                configToInfo.forEach((transformConfig, transformerInfo) ->
                        transformerInfo.getTargetClassConfigList()
                                .forEach(
                                        targetClassConfig -> func.exec(
                                                moduleConfig.getContextPath(),
                                                MethodFinder.getInstance().find(targetClassConfig)
                                        )
                                )
                )
        );
    }

    public ClassFinder getClassFinder() {
        return PluginFactory.getInstance().find(ClassFinder.class,
                AppTypePluginFilter.getInstance()
        );
    }

    public List<TargetClassConfig> convert(String context, List<ClassConfig> classConfigList) {
        List<TargetClassConfig> targetClassConfigList = new ArrayList<>();
        ClassFinder classFinder = getClassFinder();
        classConfigList.forEach(classConfig -> {
            Class<?> targetClass = classFinder.findClass(context, classConfig.getTargetClass());
            if (targetClass.isInterface())
                logger.warn("Interface class can not be transformed: {}" + targetClass.getName());
            else
                targetClassConfigList.add(new TargetClassConfig(targetClass, classConfig));
        });
        return targetClassConfigList;
    }

    private Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> parseConfig(ConfigItem item) {
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = new HashMap<>();
        for (ModuleConfig moduleConfig : ConfigParseFactory.parse(item)) {
            Map<TransformConfig, TransformerInfo> configToInfo = new HashMap<>();
            for (TransformConfig transformConfig : moduleConfig.getTransformConfigList()) {
                String context = moduleConfig.getContextPath();
                configToInfo.put(transformConfig,
                        new TransformerInfo(context,
                                convert(context, transformConfig.getTargetList())
                        )
                );
            }
            rsMap.put(moduleConfig, configToInfo);
        }
        return rsMap;
    }

    private ConfigTransformer newTransformer(TransformerConfig transformerConfig) {
        return Utils.wrapToRtError(
                () -> {
                    String className = transformerConfig.getImplClass();
                    if (className != null)
                        return ConfigTransformer.class.cast(
                                ReflectionUtils.findClass(className).newInstance()
                        );
                    return TransformerClassRegistry.get(transformerConfig.getRef()).newInstance();
                },
                () -> "Create transformer failed."
        );
    }

    public List<TransformResult> transform(List<TransformContext> transformContextList) {
        return transformContextList.stream()
                .map(transformContext -> {
                    TransformResult result = transformContext.doTransform();
                    this.reTransformClasses(
                            result.getTransformedClassSet(),
                            Collections.singleton(
                                    new UpdateClassDataTransformer(result)
                            ),
                            result::addReTransformError
                    );
                    Map<Class<?>, byte[]> classDataMap = result.getReTransformedClassData();
                    ClassDataRepository.getInstance().saveClassData(classDataMap);
                    if (!transformContext.isSkipRecordClass())
                        updateClassCache(
                                transformContext.context,
                                new HashSet<>(
                                        classDataMap.keySet()
                                )
                        );
                    return result;
                })
                .collect(Collectors.toList());
    }

    public <T extends ClassFileTransformer> void reTransformClasses(Collection<Class<?>> classes, Collection<T> transformers,
                                                                    ReTransformClassErrorHandler errorHandler) {
        transformers.forEach(transformer -> instrumentation.addTransformer(transformer, true));
        try {
            classes.forEach(clazz -> {
                try {
                    instrumentation.retransformClasses(clazz);
                } catch (Throwable t) {
                    errorHandler.handle(clazz, t);
                }
            });
        } finally {
            transformers.forEach(instrumentation::removeTransformer);
        }
    }

    private void updateClassCache(String context, Set<Class<?>> reTransformedClassSet) {
        classLock.sync(lock ->
                contextToTransformedClassSet.computeIfAbsent(
                        context,
                        key -> new HashSet<>()
                ).addAll(reTransformedClassSet)
        );
    }

    private List<TransformContext> newResetContexts(String contextExpr, Set<String> classExprSet) {
//        logger.debug("Reset context expr: {}, class expr set: {}", contextExpr, classExprSet);
//        Pattern contextPattern = contextExpr == null ? null : Pattern.compile(contextExpr);
//        List<Pattern> classPatterns = classExprSet == null || classExprSet.isEmpty() ?
//                null :
//                classExprSet.stream()
//                        .map(Pattern::compile)
//                        .collect(Collectors.toList());
        List<TransformContext> transformContextList = new ArrayList<>();
//        classLock.sync(lock ->
//                contextToTransformedClassSet.forEach((context, classSet) -> {
//                    if (contextPattern == null || contextPattern.matcher(context).matches()) {
//                        Set<Class<?>> resetClassSet = new HashSet<>();
//                        classSet.forEach(clazz -> {
//                            if (classPatterns == null ||
//                                    classPatterns.stream().anyMatch(
//                                            classPattern -> classPattern.matcher(clazz.getName()).matches())
//                                    ) {
//                                logger.debug("Add to reset, context: {}, class: {}", context, clazz);
//                                resetClassSet.add(clazz);
//                            }
//                        });
//                        transformContextList.add(
//                                new TransformContext(
//                                        context,
//                                        resetClassSet,
//                                        Collections.singletonList(
//                                                new ResetClassTransformer(
//                                                        getDynamicClassLoader(context),
//                                                        resetClassSet
//                                                )
//                                        ),
//                                        true
//                                )
//                        );
//                    }
//                })
//        );
        return transformContextList;
    }

//    private void cleanAfterReset(List<TransformResult> rsList) {
//        classLock.sync(lock ->
//                rsList.forEach(transformResult -> {
//                    if (transformResult.isSuccess()) {
//                        TransformContext transformContext = transformResult.transformContext;
//                        final String context = transformContext.context;
//                        Set<Class<?>> transformedClassSet = Optional.ofNullable(contextToTransformedClassSet.get(context))
//                                .orElseThrow(() -> new RuntimeException("No context found: " + context));
//                        transformedClassSet.removeAll(transformContext.classSet);
//                        if (transformedClassSet.isEmpty()) {
//                            logger.debug("No transformed class left, remove context: {}", context);
//                            contextToTransformedClassSet.remove(context);
//                        }
//                        EventListenerMgr.fireEvent(
//                                new ResetClassEvent(
//                                        transformContext,
//                                        contextToTransformedClassSet.isEmpty()
//                                )
//                        );
//                    }
//                })
//        );
//    }

    public List<TransformResult> resetAllClasses() {
        return resetClasses(null, null);
    }

    public List<TransformResult> resetClasses(String contextExpr, Set<String> classExprSet) {
//        List<TransformContext> transformContextList = newResetContexts(contextExpr, classExprSet);
//        if (transformContextList.isEmpty()) {
//            logger.debug("No class need to reset.");
//            return Collections.emptyList();
//        } else {
//            List<TransformResult> rsList = transform(transformContextList);
//            cleanAfterReset(rsList);
//            return rsList;
//        }
        return Collections.emptyList();
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

    public Map<String, Set<URL>> getContextToClasspathSet() {
        return loaderLock.syncValue(lock -> {
            Map<String, Set<URL>> rsMap = new HashMap<>();
            contextToDynamicClassLoader.forEach((context, classLoader) -> {
                        Set<URL> urls = classLoader.getURLs();
                        if (!urls.isEmpty())
                            rsMap.put(context, urls);
                    }
            );
            return rsMap;
        });
    }

    public void addClasspath(String context, URL url) {
        getDynamicClassLoader(context).addURL(url);
    }

    public void removeClasspath(String context, URL url) {
        getDynamicClassLoader(context).removeURL(url);
    }

    public void clearClasspath(String context) {
        getDynamicClassLoader(context).clear();
    }

    public void refreshClasspath(String context) {
        getDynamicClassLoader(context).refreshAll();
    }

    public void refreshClasspath(String context, URL url) {
        getDynamicClassLoader(context).refreshURL(url);
    }

    public interface SearchFunc {
        void exec(String context, MethodSearchResult result);
    }

    public interface ReTransformClassErrorHandler {
        void handle(Class<?> clazz, Throwable e);
    }
}
