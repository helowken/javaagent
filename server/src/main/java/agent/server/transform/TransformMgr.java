package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.plugin.ClassFinder;
import agent.server.ServerListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.TransformClassEvent;
import agent.server.transform.InvokeFinder.InvokeSearchResult;
import agent.server.transform.cache.ClassCache;
import agent.server.transform.cache.ClassFilter;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.ConfigParseFactory;
import agent.server.transform.impl.ClassesToConfig;
import agent.server.transform.impl.TransformShareInfo;
import agent.server.transform.impl.UpdateClassDataTransformer;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

import static agent.hook.utils.App.getClassFinder;
import static agent.server.transform.TransformContext.ACTION_MODIFY;

public class TransformMgr implements ServerListener {
    private static final Logger logger = Logger.getLogger(TransformMgr.class);
    private static TransformMgr instance = new TransformMgr();
    private Instrumentation instrumentation;

    public static TransformMgr getInstance() {
        return instance;
    }

    private TransformMgr() {
    }

    @Override
    public void onStartup(Object[] args) {
        this.instrumentation = Utils.getArgValue(args, 0);
    }

    @Override
    public void onShutdown() {
    }

    public Class<?>[] getInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }

    public Class<?>[] getAllLoadedClasses() {
        return instrumentation.getAllLoadedClasses();
    }

    public List<TransformResult> transformByConfig(ConfigItem configItem) {
        List<TransformContext> transformContextList = new ArrayList<>();
        Map<String, Map<TransformConfig, TransformShareInfo>> rsMap = parseConfig(configItem);
        rsMap.forEach(
                (contextPath, configToInfo) -> {
                    Set<Class<?>> classSet = new HashSet<>();
                    List<AgentTransformer> transformerList = new ArrayList<>();
                    configToInfo.forEach(
                            (transformConfig, transformShareInfo) -> {
                                classSet.addAll(transformShareInfo.getTargetClasses());
                                transformConfig.getTransformers().forEach(
                                        transformerConfig -> {
                                            ConfigTransformer transformer = newTransformer(transformerConfig);
                                            transformer.setTransformerInfo(transformShareInfo);
                                            transformer.setConfig(transformerConfig.getConfig());
                                            transformerList.add(transformer);
                                        }
                                );
                            }
                    );
                    transformContextList.add(
                            new TransformContext(
                                    contextPath,
                                    classSet,
                                    transformerList,
                                    ACTION_MODIFY
                            )
                    );
                }
        );
        return transform(transformContextList);
    }

    public void searchInvokes(ConfigItem item, SearchFunc func) {
        Map<String, Map<TransformConfig, TransformShareInfo>> rsMap = parseConfig(item);
        rsMap.forEach(
                (contextPath, configToInfo) -> configToInfo.values().forEach(
                        transformShareInfo -> transformShareInfo.getClassToInvokeFilters().forEach(
                                (clazz, invokeFilterConfigs) -> func.exec(
                                        contextPath,
                                        InvokeFinder.getInstance().find(clazz, invokeFilterConfigs)
                                )
                        )
                )
        );
    }

    public ClassesToConfig newClassesToInvokeFilter(String context, List<ClassConfig> classConfigList, ClassCache classCache) {
        ClassesToConfig classesToConfig = new ClassesToConfig();
        classConfigList.forEach(
                classConfig -> {
                    Set<Class<?>> classSet = new HashSet<>();
                    Optional.ofNullable(
                            classConfig.getTargetClasses()
                    ).ifPresent(
                            targetClasses -> collectTargetClasses(context, targetClasses, classCache, classSet)
                    );

                    Optional.ofNullable(
                            classConfig.getIncludeClasses()
                    ).ifPresent(
                            includes -> collectIncludeClasses(context, includes, classCache, classSet)
                    );

                    if (!classSet.isEmpty())
                        classesToConfig.add(classSet, classConfig);
                }
        );
        return classesToConfig;
    }

    private void collectIncludeClasses(String context, Collection<String> includes, ClassCache classCache, Set<Class<?>> classSet) {
        classSet.addAll(
                classCache.findClasses(
                        getClassFinder().findClassLoader(context),
                        includes,
                        false
                )
        );
    }

    private void collectTargetClasses(String context, Collection<String> classNames, ClassCache classCache, Set<Class<?>> classSet) {
        ClassFinder classFinder = getClassFinder();
        ClassLoader loader = classFinder.findClassLoader(context);
        classNames.forEach(
                className -> {
                    Class<?> clazz = classFinder.findClass(context, className);
                    if (clazz.isInterface())
                        classSet.addAll(
                                classCache.getSubTypes(loader, clazz, false)
                        );
                    else
                        classSet.add(clazz);
                }
        );
    }

    private ClassCache newClassCache(Collection<ModuleConfig> moduleConfigs) {
        ClassFinder classFinder = getClassFinder();
        Map<ClassLoader, ClassFilter> loaderToFilter = new HashMap<>();
        moduleConfigs.forEach(
                moduleConfig -> {
                    ClassLoader loader = classFinder.findClassLoader(
                            moduleConfig.getContextPath()
                    );
                    if (loaderToFilter.containsKey(loader))
                        throw new RuntimeException("Duplicated context path: " + moduleConfig.getContextPath());
                    loaderToFilter.put(
                            loader,
                            ClassCache.newClassFilter(
                                    moduleConfig.getIncludePackages(),
                                    moduleConfig.getExcludePackages(),
                                    true
                            )
                    );
                }
        );
        return new ClassCache(loaderToFilter);
    }

    private Map<String, Map<TransformConfig, TransformShareInfo>> parseConfig(ConfigItem item) {
        List<ModuleConfig> moduleConfigList = ConfigParseFactory.parse(item);
        ClassCache classCache = newClassCache(moduleConfigList);
        return moduleConfigList.stream()
                .collect(
                        Collectors.toMap(
                                ModuleConfig::getContextPath,
                                moduleConfig -> moduleConfig.getTransformConfigs()
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        transformConfig -> transformConfig,
                                                        transformConfig -> newTransformerInfo(
                                                                moduleConfig.getContextPath(),
                                                                transformConfig,
                                                                classCache
                                                        )
                                                )
                                        )
                        )
                );
    }

    private TransformShareInfo newTransformerInfo(String context, TransformConfig transformConfig, ClassCache classCache) {
        return new TransformShareInfo(
                context,
                newClassesToInvokeFilter(
                        context,
                        transformConfig.getTargets(),
                        classCache
                ),
                classCache
        );
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
                .map(this::doTransform)
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

    public interface SearchFunc {
        void exec(String context, InvokeSearchResult result);
    }

    public interface ReTransformClassErrorHandler {
        void handle(Class<?> clazz, Throwable e);
    }

    private TransformResult doTransform(TransformContext transformContext) {
        TransformResult transformResult = new TransformResult(
                transformContext.getContext()
        );
        List<ProxyRegInfo> regInfos = prepareRegInfos(transformContext, transformResult);
        List<ProxyResult> proxyResults = compile(regInfos, transformResult);
        Map<Class<?>, byte[]> classToData = reTransform(transformResult, proxyResults);
        Set<Class<?>> validClassSet = new HashSet<>(
                classToData.keySet()
        );
        regValidProxyResults(proxyResults, validClassSet);

        ClassDataRepository.getInstance().saveClassData(classToData);
        EventListenerMgr.fireEvent(
                new TransformClassEvent(
                        transformContext.getContext(),
                        transformContext.getAction(),
                        validClassSet
                )
        );
        return transformResult;
    }

    private List<ProxyRegInfo> prepareRegInfos(TransformContext transformContext, TransformResult transformResult) {
        transformContext.getTransformerList().forEach(
                transformer -> {
                    try {
                        transformer.transform(transformContext);
                    } catch (Throwable t) {
                        logger.error("transform failed.", t);
                        transformResult.addTransformError(t, transformer);
                    }
                }
        );
        List<ProxyRegInfo> regInfos = new LinkedList<>();
        transformContext.getTransformerList().stream()
                .map(AgentTransformer::getProxyRegInfos)
                .forEach(regInfos::addAll);
        return regInfos;
    }

    private List<ProxyResult> compile(List<ProxyRegInfo> regInfos, TransformResult transformResult) {
        List<ProxyResult> rsList = new ArrayList<>();
        ProxyTransformMgr.getInstance().transform(
                regInfos,
                ClassDataRepository.getInstance()::getClassData
        ).forEach(
                proxyResult -> {
                    if (proxyResult.hasError())
                        transformResult.addCompileError(
                                proxyResult.getTargetClass(),
                                proxyResult.getError()
                        );
                    else
                        rsList.add(proxyResult);
                }
        );
        return rsList;
    }

    private Map<Class<?>, byte[]> reTransform(TransformResult transformResult, List<ProxyResult> proxyResults) {
        Map<Class<?>, byte[]> classToData = new HashMap<>();
        proxyResults.forEach(
                proxyResult -> classToData.put(
                        proxyResult.getTargetClass(),
                        proxyResult.getClassData()
                )
        );
        Set<Class<?>> failedClasses = new HashSet<>();
        reTransformClasses(
                new HashSet<>(
                        classToData.keySet()
                ),
                Collections.singleton(
                        new UpdateClassDataTransformer(classToData)
                ),
                (clazz, error) -> {
                    failedClasses.add(clazz);
                    transformResult.addReTransformError(clazz, error);
                }
        );
        failedClasses.forEach(classToData::remove);
        return classToData;
    }

    private void regValidProxyResults(List<ProxyResult> originalProxyResults, Set<Class<?>> validClassSet) {
        ProxyTransformMgr.getInstance().reg(
                originalProxyResults.stream()
                        .filter(
                                proxyResult -> validClassSet.contains(
                                        proxyResult.getTargetClass()
                                )
                        )
                        .collect(
                                Collectors.toList()
                        )
        );
    }

}
