package test.server;

import agent.base.plugin.PluginFactory;
import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.common.utils.JSONUtils;
import agent.delegate.JSONDelegate;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.transform.AgentTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;
import org.junit.BeforeClass;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

public abstract class AbstractTest {
    private static final TestClassLoader loader = new TestClassLoader();
    private static final TestClassFinder classFinder = new TestClassFinder();
    private static boolean inited = false;
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
            DestInvokeIdRegistry.getInstance().onStartup(new Object[0]);
            inited = true;
        }
    }

    protected void doTransform(AbstractConfigTransformer transformer, String context, Map<String, Object> config, Map<Class<?>, String> classToMethodFilter) throws Exception {
        transformer.setTransformerInfo(
                newTransformerInfo(context, classToMethodFilter)
        );
        transformer.setConfig(config);
        transformer.transform(
                newTransformContext(
                        context,
                        transformer,
                        classToMethodFilter.keySet().toArray(new Class[0])
                )
        );
    }

    protected Object newInstance(Map<Class<?>, byte[]> classToData, Class<?> clazz) throws Exception {
        Class<?> newClass = loader.loadClass(
                clazz.getName(),
                classToData.get(clazz)
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

    protected void flushAndWaitMetadata() throws Exception {
        waitMetadataListener.clear();
        EventListenerMgr.fireEvent(
                new FlushLogEvent()
        );
        waitMetadataListener.await();
    }

    protected Map<Class<?>, byte[]> getClassToData(AgentTransformer transformer) {
        Collection<ProxyRegInfo> regInfos = transformer.getProxyRegInfos();
        List<ProxyResult> results = ProxyTransformMgr.getInstance().transform(
                regInfos,
                clazz -> Utils.wrapToRtError(
                        () -> IOUtils.readBytes(
                                ClassLoader.getSystemResourceAsStream(clazz.getName().replace('.', '/') + ".class")
                        )
                )
        );
        ProxyTransformMgr.getInstance().reg(results);
        return results.stream().collect(
                Collectors.toMap(
                        ProxyResult::getTargetClass,
                        ProxyResult::getClassData
                )
        );
    }

    private TransformContext newTransformContext(String context, AgentTransformer transformer, Class<?>... classes) {
        assertNotNull(classes);
        Set<Class<?>> classSet = new HashSet<>(
                Arrays.asList(
                        classes
                )
        );
        return new TransformContext(
                context,
                classSet,
                Collections.singletonList(transformer),
                TransformContext.ACTION_MODIFY
        );
    }

    private TransformerInfo newTransformerInfo(String context, Map<Class<?>, String> classToMethodFilter) {
        List<ClassConfig> classConfigs = new ArrayList<>();
        classToMethodFilter.forEach(
                (clazz, methodFilter) -> classConfigs.add(
                        ClassConfig.newInstance(
                                clazz.getName(),
                                MethodFilterConfig.includes(
                                        Collections.singleton(methodFilter)
                                )
                        )
                )
        );
        return new TransformerInfo(
                context,
                TransformMgr.getInstance().convert(context, classConfigs)
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
                () -> new RuntimeException("No method found by name: " + name)
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
