package test.server;

import agent.base.plugin.PluginFactory;
import agent.base.utils.*;
import agent.common.config.*;
import agent.common.utils.JSONUtils;
import agent.delegate.JSONDelegate;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.jvmti.JvmtiUtils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.event.impl.ResetEvent;
import agent.server.transform.*;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;
import org.junit.BeforeClass;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agent.server.transform.TransformContext.ACTION_MODIFY;
import static org.junit.Assert.assertFalse;

public abstract class AbstractTest {
    protected static final TestClassLoader loader = new TestClassLoader();
    private static final TestClassFinder classFinder = new TestClassFinder();
    private static boolean inited = false;
    private static final TestInstrumentation instrumentation = new TestInstrumentation();
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

    private static synchronized void init() throws Exception {
        if (!inited) {
            App.instance = new Object();
            SystemConfig.load(new Properties());
            PluginFactory.setMock(ClassFinder.class, classFinder);
            ReflectionUtils.useField(
                    JSONUtils.class,
                    "delegateClass",
                    field -> {
                        field.set(null, JSONDelegate.class);
                        return null;
                    }
            );
            TransformMgr.getInstance().onStartup(new Object[]{instrumentation});
            DestInvokeIdRegistry.getInstance().onStartup(new Object[0]);
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

    protected void doTransform(ConfigTransformer transformer, String context, Map<String, Object> config,
                               Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        doTransform(
                Collections.singletonMap(transformer, config),
                context,
                classToMethodFilter,
                invokeChainConfig
        );
    }

    protected void doTransform(Map<ConfigTransformer, Map<String, Object>> transformerToConfig, String context,
                               Map<Class<?>, String> classToMethodFilter, InvokeChainConfig invokeChainConfig) throws Exception {
        TransformContext transformContext = newTransformContext(
                context,
                new ArrayList<>(
                        transformerToConfig.keySet()
                ),
                classToMethodFilter,
                invokeChainConfig
        );
        for (Map.Entry<ConfigTransformer, Map<String, Object>> entry : transformerToConfig.entrySet()) {
            ConfigTransformer transformer = entry.getKey();
            Map<String, Object> config = entry.getValue();
            transformer.setContext(context);
            transformer.setConfig(config);
            transformer.transform(transformContext);
        }
    }

    protected static void transformByAnnt(String context, Map<Class<?>, String> classToMethodFilter,
                                          Map<Class<?>, String> classToConstructorFilter, Object instance) {
        TestAnnotationConfigTransformer transformer = new TestAnnotationConfigTransformer(instance);
        Set<DestInvoke> invokeSet = TransformMgr.getInstance().searchInvokes(
                newModuleConfig(context, classToMethodFilter, classToConstructorFilter, null)
        );
        TransformMgr.getInstance().registerInvokes(context, invokeSet);
        TransformContext transformContext = new TransformContext(
                context,
                invokeSet,
                Collections.singletonList(transformer),
                ACTION_MODIFY
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

    protected void flushAndWaitData() throws Exception {
        waitDataListener.clear();
        EventListenerMgr.fireEvent(
                new FlushLogEvent()
        );
        waitDataListener.await();
    }

    protected void flushAndWaitMetadata(String outputPath) throws Exception {
        waitMetadataListener.clear();
        EventListenerMgr.fireEvent(
                new FlushLogEvent(outputPath)
        );
        waitMetadataListener.await();
    }

    protected void resetAll(String context) throws Exception {
        EventListenerMgr.fireEvent(
                new ResetEvent(context, true)
        );
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
                                ProxyResult::getClassData
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

    private static ModuleConfig newModuleConfig(String context, Map<Class<?>, String> classToMethodFilter,
                                                Map<Class<?>, String> classToConstructorFilter, InvokeChainConfig invokeChainConfig) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(context);
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

    private TransformContext newTransformContext(String context, List<AgentTransformer> transformers, Map<Class<?>, String> classToMethodFilter,
                                                 InvokeChainConfig invokeChainConfig) {
        Set<DestInvoke> invokeSet = TransformMgr.getInstance().searchInvokes(
                newModuleConfig(context, classToMethodFilter, null, invokeChainConfig)
        );
        TransformMgr.getInstance().registerInvokes(context, invokeSet);
        return new TransformContext(
                context,
                invokeSet,
                transformers,
                ACTION_MODIFY
        );
    }

    protected void runWithFile(RunFileFunc runFileFunc) throws Exception {
        Path path = Files.createTempFile("test-", ".log");
        File logFile = path.toFile();
        String outputPath = logFile.getAbsolutePath();
        try {
            Map<String, Object> logConf = new HashMap<>();
            logConf.put("outputPath", outputPath);
            Map<String, Object> config = Collections.singletonMap("log", logConf);
            runFileFunc.run(outputPath, config);
        } finally {
            Files.delete(path);
            new File(outputPath + DestInvokeIdRegistry.METADATA_FILE).delete();
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
