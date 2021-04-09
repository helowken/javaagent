package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.TimeMeasureUtils;
import agent.base.utils.Utils;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.invoke.DestInvoke;
import agent.invoke.proxy.ProxyRegInfo;
import agent.invoke.proxy.ProxyResult;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.ClassSearcher;
import agent.server.transform.search.InvokeChainSearcher;
import agent.server.transform.search.InvokeSearcher;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.lang.instrument.ClassDefinition;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


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
                .map(this::getOrCreateTransformer)
                .collect(
                        Collectors.toList()
                );

        return transform(
                new TransformContext(invokeSet, transformerList)
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
                                                            ClassDataRepository.getInstance()::getCurrentClassData,
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

    private ConfigTransformer getOrCreateTransformer(TransformerConfig transformerConfig) {
        return Utils.wrapToRtError(
                () -> TransformerRegistry.getOrCreateTransformer(transformerConfig),
                () -> "Create transformer failed."
        );
    }

    public TransformResult transform(TransformContext transformContext) {
        TransformResult transformResult = new TransformResult();
        List<ProxyRegInfo> regInfos = TimeMeasureUtils.run(
                () -> prepareRegInfos(transformContext, transformResult),
                "time-pre-transform: {}"
        );
        List<ProxyResult> proxyResults = TimeMeasureUtils.run(
                () -> compile(regInfos, transformResult),
                "time-bytecode-compile: {}"
        );
        if (!proxyResults.isEmpty()) {
            List<ProxyResult> validRsList = TimeMeasureUtils.run(
                    () -> reTransform(transformResult, proxyResults),
                    "time-instrument-retransform: {}"
            );
            TimeMeasureUtils.run(
                    () -> ProxyTransformMgr.getInstance().reg(validRsList),
                    "time-proxy-register: {}"
            );
        } else {
            logger.debug("No class need to be retransformed.");
        }
        return transformResult;
    }

    private List<ProxyRegInfo> prepareRegInfos(TransformContext transformContext, TransformResult transformResult) {
        List<ProxyRegInfo> regInfos = new LinkedList<>();
        transformContext.getTransformerList().forEach(
                transformer -> {
                    try {
                        transformer.init();
                        transformer.transform(transformContext);
                        regInfos.addAll(
                                transformer.getProxyRegInfos()
                        );
                    } catch (Throwable t) {
                        logger.error("transform failed.", t);
                        transformResult.addTransformError(t, transformer.getRegKey());
                    }
                }
        );
        return regInfos;
    }

    private List<ProxyResult> compile(List<ProxyRegInfo> regInfos, TransformResult transformResult) {
        List<ProxyResult> rsList = new ArrayList<>();
        ProxyTransformMgr.getInstance().transform(
                regInfos,
                ClassDataRepository.getInstance()::getCurrentClassData
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

    private List<ProxyResult> reTransform(TransformResult transformResult, List<ProxyResult> proxyResults) {
        return doReTransform(
                transformResult,
                proxyResults,
                ProxyResult::getTargetClass,
                ClassDataRepository.getInstance()::getCurrentClassData
        );
    }

    public <P> List<P> doReTransform(TransformResult transformResult, List<P> inputList, Function<P, Class<?>> convertFunc, Function<Class<?>, byte[]> classDataFunc) {
        List<P> validRsList = new ArrayList<>();
        for (P input : inputList) {
            Class<?> clazz = convertFunc.apply(input);
            try {
                byte[] classData = classDataFunc.apply(clazz);
                ClassDefinition newClassDef = new ClassDefinition(clazz, classData);
                InstrumentationMgr.getInstance().run(
                        instrumentation -> instrumentation.redefineClasses(newClassDef)
                );
                validRsList.add(input);
            } catch (Throwable t) {
                transformResult.addReTransformError(clazz, t);
                logger.error("Update class data failed: {}", t, clazz);
            }
        }
        return validRsList;
    }
}
