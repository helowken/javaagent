package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.AppTypePluginFilter;
import agent.server.ServerListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.TransformClassEvent;
import agent.server.transform.MethodFinder.MethodSearchResult;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.TransformConfig;
import agent.server.transform.config.TransformerConfig;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.ConfigParseFactory;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.UpdateClassDataTransformer;
import agent.server.transform.revision.ClassDataRepository;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

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
        Map<ModuleConfig, Map<TransformConfig, TransformerInfo>> rsMap = parseConfig(configItem);
        rsMap.forEach((moduleConfig, configToInfo) -> {
                    Set<Class<?>> classSet = new HashSet<>();
                    List<AgentTransformer> transformerList = new ArrayList<>();
                    configToInfo.forEach((transformConfig, transformerInfo) -> {
                        classSet.addAll(transformerInfo.getTargetClassSet());
                        for (TransformerConfig transformerConfig : transformConfig.getTransformers()) {
                            ConfigTransformer transformer = newTransformer(transformerConfig);
                            transformer.setTransformerInfo(transformerInfo);
                            transformer.setConfig(transformerConfig.getConfig());
                            transformerList.add(transformer);
                        }
                    });
                    transformContextList.add(
                            new TransformContext(
                                    moduleConfig.getContextPath(),
                                    classSet,
                                    transformerList,
                                    ACTION_MODIFY
                            )
                    );
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
            for (TransformConfig transformConfig : moduleConfig.getTransformConfigs()) {
                String context = moduleConfig.getContextPath();
                configToInfo.put(transformConfig,
                        new TransformerInfo(context,
                                convert(context, transformConfig.getTargets())
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
                    EventListenerMgr.fireEvent(
                            new TransformClassEvent(
                                    transformContext.context,
                                    transformContext.getAction(),
                                    new HashSet<>(
                                            classDataMap.keySet()
                                    )
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

    public interface SearchFunc {
        void exec(String context, MethodSearchResult result);
    }

    public interface ReTransformClassErrorHandler {
        void handle(Class<?> clazz, Throwable e);
    }
}
