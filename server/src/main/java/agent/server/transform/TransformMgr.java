package agent.server.transform;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.TransformClassEvent;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static agent.hook.utils.App.getLoader;
import static agent.server.transform.TransformContext.ACTION_MODIFY;

public class TransformMgr {
    private static final Logger logger = Logger.getLogger(TransformMgr.class);
    private static TransformMgr instance = new TransformMgr();

    public static TransformMgr getInstance() {
        return instance;
    }

    private TransformMgr() {
    }

    public void registerInvokes(String context, Collection<DestInvoke> invokes) {
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

    public TransformResult transformByConfig(ModuleConfig moduleConfig) {
        moduleConfig.validate();
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

    public Set<DestInvoke> searchInvokes(ModuleConfig moduleConfig) {
        long st = System.currentTimeMillis();
        try {
            moduleConfig.validateForSearch();
            ClassLoader loader = getLoader(
                    moduleConfig.getContextPath()
            );
            ClassCache classCache = new ClassCache(loader);
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
            logger.debug("====== Found invokes: ");
            invokeSet.forEach(
                    invoke -> logger.debug("{}", invoke.toString())
            );
            logger.debug("==================");
            return invokeSet;
        } finally {
            long et = System.currentTimeMillis();
            logger.debug("searchTime: {}", (et - st));
        }
    }

    private ConfigTransformer newTransformer(TransformerConfig transformerConfig) {
        return Utils.wrapToRtError(
                () -> TransformerClassRegistry.newTransformer(
                        transformerConfig.getRef()
                ),
                () -> "Create transformer failed."
        );
    }

    public TransformResult transform(TransformContext transformContext) {
        TransformResult transformResult = new TransformResult(
                transformContext.getContext()
        );
        long t1 = System.currentTimeMillis();
        List<ProxyRegInfo> regInfos = prepareRegInfos(transformContext, transformResult);
        long t2 = System.currentTimeMillis();
        logger.debug("t1: {}", (t2 - t1));
        List<ProxyResult> proxyResults = compile(regInfos, transformResult);
        long t3 = System.currentTimeMillis();
        logger.debug("t2: {}", (t3 - t2));
        if (!proxyResults.isEmpty()) {
            Map<Class<?>, byte[]> classToData = reTransform(transformResult, proxyResults);
            long t4 = System.currentTimeMillis();
            logger.debug("t3: {}", (t4 - t3));
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
            long t5 = System.currentTimeMillis();
            logger.debug("t4: {}", (t5 - t4));
        } else {
            logger.debug("No class need to be retransformed.");
        }
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
        Map<Class<?>, byte[]> classToData = new ConcurrentHashMap<>();
        proxyResults.forEach(
                proxyResult -> classToData.put(
                        proxyResult.getTargetClass(),
                        proxyResult.getClassData()
                )
        );
        try {
            InstrumentationMgr.getInstance().retransform(
                    new UpdateClassDataTransformer(classToData),
                    classToData.keySet().toArray(new Class[0])
            );
            return classToData;
        } catch (Throwable t) {
            transformResult.addReTransformError(Object.class, t);
            logger.error("Update class data failed.", t);
            return Collections.emptyMap();
        }
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
