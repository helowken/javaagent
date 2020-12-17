package test.server;

import agent.base.utils.*;
import agent.common.config.*;
import agent.common.utils.DependentClassItem;
import agent.common.utils.JsonUtils;
import agent.common.utils.MetadataUtils;
import agent.invoke.DestInvoke;
import agent.invoke.MethodInvoke;
import agent.invoke.proxy.ProxyRegInfo;
import agent.invoke.proxy.ProxyResult;
import agent.jvmti.JvmtiUtils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.transform.*;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.tools.asm.AsmUtils;
import agent.server.transform.tools.asm.ProxyTransformMgr;
import agent.tools.asm.AsmDelegate;
import agent.tools.json.JsonDelegate;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

public abstract class AbstractTest {
    protected static final TestClassLoader loader = new TestClassLoader();
    private static boolean inited = false;
    protected static final TestInstrumentation instrumentation = new TestInstrumentation();
    private static final WaitFlushingListener waitMetadataListener = new WaitFlushingListener(DestInvokeMetadataFlushedEvent.class);
    private static final WaitFlushingListener waitDataListener = new WaitFlushingListener(LogFlushedEvent.class);

    static {
        EventListenerMgr.reg(LogFlushedEvent.class, waitDataListener);
        EventListenerMgr.reg(DestInvokeMetadataFlushedEvent.class, waitMetadataListener);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ClassDataRepository.getInstance().clearAllData();
    }

    private static synchronized void init() throws Exception {
        if (!inited) {
            Properties props = new Properties();
            props.setProperty("invoke.chain.search.cache.max.size", "1000");
            props.setProperty("invoke.chain.search.core.pool.size", "1");
            props.setProperty("invoke.chain.search.max.pool.size", "100");
            SystemConfig.load(props, Collections.emptyMap());

            DependentClassItem.getInstance().mock(AsmUtils.ASM_DELEGATE_CLASS, AsmDelegate.class);
            DependentClassItem.getInstance().mock(JsonUtils.JSON_DELEGATE_CLASS, JsonDelegate.class);

            Object[] args = new Object[]{instrumentation};
            InstrumentationMgr.getInstance().onStartup(args);
            ProxyTransformMgr.getInstance().onStartup(args);
            String dir = System.getProperty("user.dir");
            String s = "javaagent";
            int pos = dir.indexOf(s);
            if (pos > -1) {
                dir = dir.substring(0, pos + s.length());
            }
            List<File> files = FileUtils.collectFiles(
                    file -> file.getName().endsWith(".so"),
                    dir
            );
            if (files.isEmpty())
                throw new RuntimeException("No .so file found!");
            JvmtiUtils.getInstance().load(
                    files.get(0).getCanonicalPath()
            );
            inited = true;
        }
    }

    protected String newTransformerKey() {
        return UUID.randomUUID().toString();
    }

    protected void doTransform(ConfigTransformer transformer, Map<String, Object> config,
                               Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        doTransform(
                Collections.singletonMap(transformer, config),
                classToMethodFilter,
                invokeChainConfig
        );
    }

    protected void doTransform(Map<ConfigTransformer, Map<String, Object>> transformerToConfig,
                               Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        TransformContext transformContext = newTransformContext(
                new ArrayList<>(
                        transformerToConfig.keySet()
                ),
                classToMethodFilter,
                invokeChainConfig
        );
        for (Map.Entry<ConfigTransformer, Map<String, Object>> entry : transformerToConfig.entrySet()) {
            ConfigTransformer transformer = entry.getKey();
            Map<String, Object> config = entry.getValue();
            transformer.setConfig(config);
            transformer.transform(transformContext);
        }
    }

    protected static void transformByAnnt(Map<Class<?>, String> classToMethodFilter,
                                          Map<Class<?>, String> classToConstructorFilter, Object instance) {
        TestAnnotationConfigTransformer transformer = new TestAnnotationConfigTransformer(instance);
        Set<DestInvoke> invokeSet = TransformMgr.getInstance().searchInvokes(
                newModuleConfig(classToMethodFilter, classToConstructorFilter, null)
        );
        invokeSet.forEach(DestInvokeIdRegistry.getInstance()::reg);
        TransformContext transformContext = new TransformContext(
                invokeSet,
                Collections.singletonList(transformer)
        );
        TransformResult transformResult = TransformMgr.getInstance().transform(transformContext);
        assertFalse(transformResult.hasError());
    }

    protected Object newInstance(Map<Class<?>, byte[]> classToData, Class<?> clazz) throws Exception {
        Class<?> newClass = classToData.containsKey(clazz) ?
                loader.loadClass(
                        clazz.getName(),
                        classToData.get(clazz)
                ) :
                loader.loadClass(
                        clazz.getName()
                );
        return ReflectionUtils.newInstance(newClass);
    }

    protected void flushAndWaitMetadata(String logKey) throws Exception {
        waitMetadataListener.clear();
        EventListenerMgr.fireEvent(
                new FlushLogEvent(logKey)
        );
        waitMetadataListener.await();
    }

    protected byte[] getClassData(Class<?> clazz) {
        return Utils.wrapToRtError(
                () -> IOUtils.readBytes(
                        ClassLoader.getSystemResourceAsStream(clazz.getName().replace('.', '/') + ".class")
                )
        );
    }

    protected Map<Class<?>, byte[]> getClassToData(AgentTransformer transformer) {
        return getClassToData(
                Collections.singletonList(transformer)
        );
    }

    protected Map<Class<?>, byte[]> getClassToData(Collection<? extends AgentTransformer> transformers) {
        Set<ProxyRegInfo> regInfos = new HashSet<>();
        transformers.forEach(
                transformer -> regInfos.addAll(
                        transformer.getProxyRegInfos()
                )
        );
        List<ProxyResult> results = ProxyTransformMgr.getInstance().transform(
                regInfos,
                this::getClassData
        );
        ProxyTransformMgr.getInstance().reg(results);
        return results.stream()
                .filter(r -> !r.hasError())
                .collect(
                        Collectors.toMap(
                                ProxyResult::getTargetClass,
                                proxyResult -> ClassDataRepository.getInstance().getCurrentClassData(
                                        proxyResult.getTargetClass()
                                )
                        )
                );
    }

    private static TargetConfig newTargetConfig(Map<Class<?>, String> classToFilter, Function<TargetConfig, FilterConfig> filterConfigSupplier,
                                                InvokeChainConfig invokeChainConfig) {
        TargetConfig targetConfig = new TargetConfig();
        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setIncludes(
                classToFilter.keySet()
                        .stream()
                        .map(Class::getName)
                        .collect(Collectors.toSet())
        );
        FilterConfig filterConfig = filterConfigSupplier.apply(targetConfig);
        filterConfig.setIncludes(
                new HashSet<>(
                        classToFilter.values()
                )
        );
        targetConfig.setClassFilter(
                classFilterConfig
        );
        if (invokeChainConfig != null)
            targetConfig.setInvokeChainConfig(invokeChainConfig);
        return targetConfig;
    }

    private static ModuleConfig newModuleConfig(Map<Class<?>, String> classToMethodFilter,
                                                Map<Class<?>, String> classToConstructorFilter, InvokeChainConfig invokeChainConfig) {
        ModuleConfig moduleConfig = new ModuleConfig();
        List<TargetConfig> targetConfigList = new ArrayList<>();
        if (classToConstructorFilter != null)
            targetConfigList.add(
                    newTargetConfig(
                            classToConstructorFilter,
                            targetConfig -> {
                                ConstructorFilterConfig config = new ConstructorFilterConfig();
                                targetConfig.setConstructorFilter(config);
                                return config;
                            },
                            invokeChainConfig
                    )
            );
        if (classToMethodFilter != null)
            targetConfigList.add(
                    newTargetConfig(
                            classToMethodFilter,
                            targetConfig -> {
                                MethodFilterConfig config = new MethodFilterConfig();
                                targetConfig.setMethodFilter(config);
                                return config;
                            },
                            invokeChainConfig
                    )
            );
        moduleConfig.setTargets(targetConfigList);

        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef("dummy");
        moduleConfig.setTransformers(
                Collections.singletonList(transformerConfig)
        );
        return moduleConfig;
    }

    private TransformContext newTransformContext(List<AgentTransformer> transformers, Map<Class<?>, String> classToMethodFilter,
                                                 InvokeChainConfig invokeChainConfig) {
        Set<DestInvoke> invokeSet = TransformMgr.getInstance().searchInvokes(
                newModuleConfig(classToMethodFilter, null, invokeChainConfig)
        );
        invokeSet.forEach(DestInvokeIdRegistry.getInstance()::reg);
        return new TransformContext(invokeSet, transformers);
    }

    protected void runWithFile(RunFileFunc runFileFunc) throws Exception {
        Path path = Files.createTempFile("test-", ".log");
        File logFile = path.toFile();
        String outputPath = logFile.getAbsolutePath();
        try {
            Map<String, Object> logConf = new HashMap<>();
            logConf.put("outputPath", outputPath);
            Map<String, Object> config = new HashMap<>();
            config.put("log", logConf);
            runFileFunc.run(outputPath, config);
        } finally {
            Files.delete(path);
            new File(outputPath + ".invoke_cache").delete();
            new File(outputPath + ".chain_cache").delete();
            Stream.of(
                    MetadataUtils.getMetadataFile(outputPath)
            ).map(File::new)
                    .forEach(File::delete);
        }
    }

    protected Method getMethod(Class<?> clazz, String name) throws Exception {
        return Optional.ofNullable(
                ReflectionUtils.findFirstMethod(clazz, name)
        ).orElseThrow(
                () -> new RuntimeException("No method found by name: " + clazz + "." + name)
        );
    }

    protected Constructor getConstructor(Class<?> clazz) throws Exception {
        return Optional.ofNullable(
                ReflectionUtils.findConstructor(clazz, null)
        ).orElseThrow(
                () -> new RuntimeException("No constructor found: " + clazz)
        );
    }

    protected MethodInvoke newMethodInvoke(Class<?> clazz, String name) throws Exception {
        return new MethodInvoke(
                getMethod(clazz, name)
        );
    }

    protected Map<String, Class<?>> loadNewClasses(Map<Class<?>, byte[]> classToData) {
        Map<String, Class<?>> nameToClass = new HashMap<>();
        classToData.forEach(
                (clazz, data) -> Utils.wrapToRtError(
                        () -> {
                            try {
                                nameToClass.put(
                                        clazz.getName(),
                                        loader.loadClass(
                                                clazz.getName(),
                                                data
                                        )
                                );
//                                System.out.println("========================");
//                                AsmUtils.print(data, System.out);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                )
        );
        return nameToClass;
    }

    protected interface RunFileFunc {
        void run(String outputPath, Map<String, Object> config) throws Exception;
    }

    private static class WaitFlushingListener implements AgentEventListener {
        private final Object lock = new Object();
        private final Class<? extends AgentEvent> eventClass;
        private volatile boolean finished = false;

        private WaitFlushingListener(Class<? extends AgentEvent> eventClass) {
            this.eventClass = eventClass;
        }

        @Override
        public void onNotify(AgentEvent event) {
            if (event.getClass().equals(this.eventClass)) {
                finished = true;
                Utils.wrapToRtError(
                        () -> {
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }
                );
            }
        }

        void await() throws Exception {
            synchronized (lock) {
                if (!finished)
                    lock.wait();
            }
        }

        void clear() {
            finished = false;
        }
    }


}
