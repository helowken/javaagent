package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.TimeMeasureUtils;
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

import static agent.server.transform.TransformContext.ACTION_MODIFY;

public class TransformMgr {
    private static final Logger logger = Logger.getLogger(TransformMgr.class);
    private static TransformMgr instance = new TransformMgr();

    public static TransformMgr getInstance() {
        return instance;
    }

    private TransformMgr() {
    }

    public TransformResult transformByConfig(ModuleConfig moduleConfig) {
        moduleConfig.validate();
        Set<DestInvoke> invokeSet = searchInvokes(moduleConfig);
        invokeSet.forEach(DestInvokeIdRegistry.getInstance()::reg);

        List<AgentTransformer> transformerList = moduleConfig.getTransformers()
                .stream()
                .map(
                        transformerConfig -> {
                            ConfigTransformer transformer = newTransformer(transformerConfig);
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
                new TransformContext(invokeSet, transformerList, ACTION_MODIFY)
        );
    }

    public Set<DestInvoke> searchInvokes(ModuleConfig moduleConfig) {
        return TimeMeasureUtils.run(
                () -> {
                    moduleConfig.validateForSearch();
                    ClassCache classCache = new ClassCache();
                    Set<DestInvoke> invokeSet = new HashSet<>();
                    Set<DestInvoke> invokesPerTarget = new HashSet<>();
                    moduleConfig.getTargets().forEach(
                            targetConfig -> {
                                invokesPerTarget.clear();
                                ClassSearcher.getInstance().search(
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
                                            invokesPerTarget.addAll(invokes);
                                            invokeSet.addAll(invokes);
                                        }
                                );

                                if (!invokesPerTarget.isEmpty())
                                    Optional.ofNullable(
                                            targetConfig.getInvokeChainConfig()
                                    ).ifPresent(
                                            invokeChainConfig -> invokeSet.addAll(
                                                    InvokeChainSearcher.search(
                                                            classCache,
                                                            ClassDataRepository.getInstance()::getClassData,
                                                            invokesPerTarget,
                                                            invokeChainConfig
                                                    )
                                            )
                                    );
                            }
                    );
                    return invokeSet;
                },
                "searchTime: {}"
        );
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
        TransformResult transformResult = new TransformResult();
        List<ProxyRegInfo> regInfos = TimeMeasureUtils.run(
                () -> prepareRegInfos(transformContext, transformResult),
                "t1: {}"
        );
        List<ProxyResult> proxyResults = TimeMeasureUtils.run(
                () -> compile(regInfos, transformResult),
                "t2: {}"
        );
        if (!proxyResults.isEmpty()) {
            Map<Class<?>, byte[]> classToData = TimeMeasureUtils.run(
                    () -> reTransform(transformResult, proxyResults),
                    "t3: {}"
            );
            TimeMeasureUtils.run(
                    () -> {
                        Set<Class<?>> validClassSet = new HashSet<>(
                                classToData.keySet()
                        );
                        regValidProxyResults(proxyResults, validClassSet);

                        ClassDataRepository.getInstance().saveClassData(classToData);
                        EventListenerMgr.fireEvent(
                                new TransformClassEvent(
                                        transformContext.getAction(),
                                        validClassSet
                                )
                        );
                    },
                    "t4: {}"
            );
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
