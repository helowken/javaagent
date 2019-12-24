package agent.server.transform.impl;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.utils.JSONUtils;
import agent.server.ServerListener;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.event.impl.ResetClassEvent;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DestInvokeIdRegistry implements ServerListener, AgentEventListener {
    public static final String METADATA_FILE = ".metadata";
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<DestInvoke, Integer> invokeToId = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final Map<String, String> outputPathToContext = new HashMap<>();

    public static DestInvokeIdRegistry getInstance() {
        return instance;
    }

    private DestInvokeIdRegistry() {
    }

    public int reg(DestInvoke destInvoke) {
        return invokeToId.computeIfAbsent(
                destInvoke,
                key -> idGen.getAndIncrement()
        );
    }

    public Integer get(DestInvoke destInvoke) {
        return Optional.ofNullable(
                invokeToId.get(destInvoke)
        ).orElseThrow(
                () -> new RuntimeException("No id found for destInvoke: " + destInvoke)
        );
    }

    public void regOutputPath(String context, String outputPath) {
        lo.sync(
                lock -> {
                    if (outputPathToContext.containsKey(outputPath))
                        throw new RuntimeException("Output path " + outputPath + " has already been used for context " + context);
                    outputPathToContext.put(outputPath, context);
                }
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
        lo.sync(
                lock -> {
                    if (event.isAllReset()) {
                        outputPathToContext.clear();
                        logger.debug("Clear all.");
                    } else {
                        String context = event.getContext();
                        Set<String> delKeys = new HashSet<>();
                        outputPathToContext.forEach((outputPath, ctx) -> {
                            if (ctx.equals(context))
                                delKeys.add(outputPath);
                        });
                        delKeys.forEach(outputPathToContext::remove);
                        logger.debug("After remove context: {}, outputPathToContext: {}", context, outputPathToContext);
                    }
                }
        );
    }

    private void handleLogFlushedEvent(LogFlushedEvent event) {
        String outputPath = event.getOutputPath();
        lo.sync(
                lock -> {
                    if (outputPathToContext.containsKey(outputPath)) {
                        String content = JSONUtils.writeAsString(
                                convertMetadata()
                        );
                        String path = outputPath + METADATA_FILE;
                        IOUtils.writeString(path, content, false);
                        logger.debug("Metadata is flushed for log: {}", outputPath);
                        EventListenerMgr.fireEvent(
                                new DestInvokeMetadataFlushedEvent(path)
                        );
                    }
                }
        );
    }

    private Map<String, Map<String, Integer>> convertMetadata() {
        Map<String, Map<String, Integer>> rsMap = new HashMap<>();
        invokeToId.forEach(
                (invoke, id) -> rsMap.computeIfAbsent(
                        invoke.getDeclaringClass().getName(),
                        key -> new HashMap<>()
                ).put(
                        invoke.getInvokeEntity().toString(),
                        id
                )
        );
        return rsMap;
    }

    @Override
    public void onStartup(Object[] args) {
        EventListenerMgr.reg(LogFlushedEvent.class, this);
        EventListenerMgr.reg(ResetClassEvent.class, this);
    }

    @Override
    public void onShutdown() {

    }
}
