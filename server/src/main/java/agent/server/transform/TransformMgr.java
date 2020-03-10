package agent.server.transform;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.ServerListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.TransformClassEvent;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.ConfigParseFactory;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.UpdateClassDataTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.ClassSearcher;
import agent.server.transform.search.InvokeChainSearcher;
import agent.server.transform.search.InvokeSearcher;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

import static agent.hook.utils.App.getLoader;
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

    private void registerInvokes(String context, Collection<DestInvoke> invokes) {
        ClassLoader contextLoader = getLoader(context);
        Map<ClassLoader, String> loaderToContext = new HashMap<>();
        invokes.forEach(
                invoke -> DestInvokeIdRegistry.getInstance().reg(
                        loaderToContext.computeIfAbsent(
                                invoke.getDeclaringClass().getClassLoader(),
                                loader -> ClassLoaderUtils.isSelfOrDescendant(contextLoader, loader) ? context : null
                        ),
                        invoke
                )
        );
    }

    public TransformResult transformByConfig(ConfigItem configItem) {
        ModuleConfig moduleConfig = parseConfig(configItem);
        String context = moduleConfig.getContextPath();
        Set<DestInvoke> invokeSet = searchInvokes(moduleConfig);
        registerInvokes(context, invokeSet);

        List<AgentTransformer> transformerList = moduleConfig.getTransformers()
                .stream()
                .map(
                        transformerConfig -> {
                            ConfigTransformer transformer = newTransformer(transformerConfig);
                            transformer.setContext(context);
                            transformer.setConfig(
                                    transformerConfig.getConfig()
                            );
                            return transformer;
                        }
                )
                .collect(
                        Collectors.toList()
                );

        return transform(
                new TransformContext(context, invokeSet, transformerList, ACTION_MODIFY)
        );
    }

    public Set<DestInvoke> searchInvokes(ConfigItem item) {
        ModuleConfig moduleConfig = parseConfig(item);
        return searchInvokes(moduleConfig);
    }

    public Set<DestInvoke> searchInvokes(ModuleConfig moduleConfig) {
        ClassLoader loader = getLoader(
                moduleConfig.getContextPath()
        );
        ClassCache classCache = new ClassCache();
        Set<DestInvoke> invokeSet = new HashSet<>();
        moduleConfig.getTargets().forEach(
                targetConfig -> ClassSearcher.getInstance().search(
                        loader,
                        classCache,
                        targetConfig.getClassFilter()
                ).forEach(
                        clazz -> {
                            Collection<DestInvoke> invokes = InvokeSearcher.getInstance()
                                    .search(
                                            clazz,
                                            targetConfig.getMethodFilter(),
                                            targetConfig.getConstructorFilter()
                                    );
                            invokeSet.addAll(invokes);

                            Optional.ofNullable(
                                    targetConfig.getInvokeChainConfig()
                            ).ifPresent(
                                    invokeChainConfig -> invokeSet.addAll(
                                            InvokeChainSearcher.search(
                                                    loader,
                                                    classCache,
                                                    ClassDataRepository.getInstance()::getClassData,
                                                    invokes,
                                                    invokeChainConfig
                                            )
                                    )
                            );
                        }
                )
        );
        return invokeSet;
    }

    private ModuleConfig parseConfig(ConfigItem item) {
        ModuleConfig moduleConfig = ConfigParseFactory.parse(item);
        moduleConfig.validate();
        return moduleConfig;
    }

    private ConfigTransformer newTransformer(TransformerConfig transformerConfig) {
        return Utils.wrapToRtError(
                () -> TransformerClassRegistry.get(
                        transformerConfig.getRef()
                ).newInstance(),
                () -> "Create transformer failed."
        );
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

    public interface ReTransformClassErrorHandler {
        void handle(Class<?> clazz, Throwable e);
    }

    public TransformResult transform(TransformContext transformContext) {
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
