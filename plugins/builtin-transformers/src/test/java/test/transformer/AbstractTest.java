package test.transformer;

import agent.base.plugin.PluginFactory;
import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.builtin.event.StatisticsMetadataFlushedEvent;
import agent.common.utils.JSONUtils;
import agent.delegate.JSONDelegate;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;
import agent.server.transform.AgentTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.ProxyResult;
import agent.server.transform.tools.asm.ProxyTransformMgr;
import org.junit.BeforeClass;

import java.util.*;
import java.util.stream.Collectors;

import static agent.server.transform.TransformContext.ACTION_MODIFY;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractTest {
    private static final TestClassLoader loader = new TestClassLoader();
    private static final TestClassFinder classFinder = new TestClassFinder();
    private static boolean inited = false;
    private static final StatisticsMetadataFlushedListener statisticsMetadataFlushedListener = new StatisticsMetadataFlushedListener();

    static {
        EventListenerMgr.reg(StatisticsMetadataFlushedEvent.class, statisticsMetadataFlushedListener);
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
            inited = true;
        }
    }

    void doTransform(AbstractConfigTransformer transformer, String context, Map<String, Object> config, Map<Class<?>, String> classToMethodFilter) throws Exception {
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

    Object newInstance(Map<Class<?>, byte[]> classToData, Class<?> clazz) throws Exception {
        Class<?> newClass = loader.loadClass(
                clazz.getName(),
                classToData.get(clazz)
        );
        return ReflectionUtils.newInstance(newClass);
    }

    void flush() throws Exception {
        statisticsMetadataFlushedListener.clear();
        EventListenerMgr.fireEvent(
                new FlushLogEvent()
        );
        statisticsMetadataFlushedListener.waitForLogFlushing();
    }

    Map<Class<?>, byte[]> getClassToData(AgentTransformer transformer) {
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
                ACTION_MODIFY
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

    private static class StatisticsMetadataFlushedListener implements AgentEventListener {
        private final Object lock = new Object();
        private volatile boolean finished = false;

        @Override
        public void onNotify(AgentEvent event) {
            if (event.getClass().equals(StatisticsMetadataFlushedEvent.class)) {
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

        void waitForLogFlushing() throws Exception {
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
