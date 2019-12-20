package agent.builtin.transformer.utils;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.builtin.event.StatisticsMetadataFlushedEvent;
import agent.common.utils.JSONUtils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.event.impl.ResetClassEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CostTimeMethodRegistry implements AgentEventListener {
    public static final String METADATA_FILE = ".metadata";
    private static final Logger logger = Logger.getLogger(CostTimeMethodRegistry.class);
    private static final CostTimeMethodRegistry instance = new CostTimeMethodRegistry();

    private final LockObject methodTypeLock = new LockObject();
    private final Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = new HashMap<>();
    private final AtomicInteger typeCounter = new AtomicInteger(0);
    private final Map<String, Set<String>> outputPathToContexts = new HashMap<>();

    public static CostTimeMethodRegistry getInstance() {
        return instance;
    }

    private CostTimeMethodRegistry() {
        EventListenerMgr.reg(LogFlushedEvent.class, this);
        EventListenerMgr.reg(ResetClassEvent.class, this);
    }

    public int reg(String context, String className, String methodFullName) {
        return methodTypeLock.syncValue(lock ->
                contextToClassToMethodToType.computeIfAbsent(
                        context,
                        contextKey -> new HashMap<>()
                ).computeIfAbsent(
                        className,
                        classKey -> new HashMap<>()
                ).computeIfAbsent(
                        methodFullName,
                        methodKey -> typeCounter.getAndIncrement()
                )
        );
    }

    public void regOutputPath(String context, String outputPath) {
        methodTypeLock.sync(
                lock -> outputPathToContexts.computeIfAbsent(
                        outputPath,
                        key -> new HashSet<>()
                ).add(context)
        );
    }

    @Override
    public void onNotify(AgentEvent event) {
        Class<?> eventType = event.getClass();
        if (eventType.equals(ResetClassEvent.class)) {
            handleResetEvent((ResetClassEvent) event);
        } else if (eventType.equals(LogFlushedEvent.class)) {
            handleLogFlushedEvent((LogFlushedEvent) event);
        } else
            throw new RuntimeException("Unsupported event type: " + eventType);
    }

    private void handleResetEvent(ResetClassEvent event) {
        methodTypeLock.sync(lock -> {
            if (event.isAllReset()) {
                outputPathToContexts.clear();
                contextToClassToMethodToType.clear();
                logger.debug("Clear all.");
            } else {
                String context = event.getContext();
                Set<String> delKeys = new HashSet<>();
                outputPathToContexts.forEach((outputPath, contexts) -> {
                    contexts.remove(context);
                    if (contexts.isEmpty())
                        delKeys.add(outputPath);
                });
                delKeys.forEach(outputPathToContexts::remove);
                logger.debug("After remove context: {}, outputPathToContexts: {}", context, outputPathToContexts);
            }
        });
    }

    private void handleLogFlushedEvent(LogFlushedEvent event) {
        String outputPath = event.getOutputPath();
        methodTypeLock.sync(lock -> {
            if (outputPathToContexts.containsKey(outputPath)) {
                String content = JSONUtils.writeAsString(contextToClassToMethodToType);
                IOUtils.writeString(outputPath + METADATA_FILE, content, false);
                logger.debug("Metadata is flushed for log: {}", outputPath);
                EventListenerMgr.fireEvent(
                        new StatisticsMetadataFlushedEvent()
                );
            }
        });
    }

}
