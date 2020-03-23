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
import agent.server.event.impl.ResetEvent;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static agent.server.utils.log.LogConfig.STDOUT;

public class DestInvokeIdRegistry implements ServerListener, AgentEventListener {
    private static final String UNKNOWN_CONTEXT = "@unknownContext@";
    private static final String METADATA_FILE = ".metadata";
    private static final String UNKNOWN_CONTEXT_METADATA_FILE = ".metadata_unctx";
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<String, Map<Class<?>, Map<DestInvoke, Integer>>> contextToClassToInvokeToId = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final Map<String, String> outputPathToContext = new HashMap<>();

    public static DestInvokeIdRegistry getInstance() {
        return instance;
    }

    public static boolean isMetadataFile(String path) {
        return path.endsWith(METADATA_FILE) ||
                path.endsWith(UNKNOWN_CONTEXT_METADATA_FILE);
    }

    public static String[] getMetadataFiles(String inputPath) {
        return new String[]{
                inputPath + METADATA_FILE,
                inputPath + UNKNOWN_CONTEXT_METADATA_FILE
        };
    }

    private DestInvokeIdRegistry() {
    }

    private String convertNullToUnknown(String context) {
        return context == null ? UNKNOWN_CONTEXT : context;
    }

    public int reg(String context, DestInvoke destInvoke) {
        return lo.syncValue(
                lock -> contextToClassToInvokeToId.computeIfAbsent(
                        convertNullToUnknown(context),
                        key -> new HashMap<>()
                ).computeIfAbsent(
                        destInvoke.getDeclaringClass(),
                        key -> new HashMap<>()
                ).computeIfAbsent(
                        destInvoke,
                        key -> idGen.getAndIncrement()
                )
        );
    }

    public Integer get(DestInvoke destInvoke) {
        return lo.syncValue(
                lock -> {
                    for (Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId : contextToClassToInvokeToId.values()) {
                        Map<DestInvoke, Integer> invokeToId = classToInvokeToId.get(
                                destInvoke.getDeclaringClass()
                        );
                        if (invokeToId != null) {
                            Integer id = invokeToId.get(destInvoke);
                            if (id != null)
                                return id;
                        }
                    }
                    throw new RuntimeException("No id found for destInvoke: " + destInvoke);
                }
        );
    }

    void regOutputPath(String sContext, String outputPath) {
        lo.sync(
                lock -> {
                    if (outputPathToContext.containsKey(outputPath))
                        throw new RuntimeException("Output path is registered: " + outputPath);
                    outputPathToContext.put(
                            outputPath,
                            convertNullToUnknown(sContext)
                    );
                }
        );
    }

    @Override
    public void onNotify(AgentEvent event) {
        Class<?> eventType = event.getClass();
        if (eventType.equals(ResetEvent.class)) {
            handleResetEvent((ResetEvent) event);
        } else if (eventType.equals(LogFlushedEvent.class)) {
            handleLogFlushedEvent((LogFlushedEvent) event);
        } else
            throw new RuntimeException("Unsupported event type: " + eventType);
    }

    private void handleResetEvent(ResetEvent event) {
        lo.sync(
                lock -> {
                    if (event.isAllReset()) {
                        outputPathToContext.clear();
                        logger.debug("Clear all.");
                    } else {
                        String context = convertNullToUnknown(
                                event.getContext()
                        );
                        Set<String> delKeys = new HashSet<>();
                        outputPathToContext.forEach(
                                (outputPath, ctx) -> {
                                    if (Objects.equals(ctx, context))
                                        delKeys.add(outputPath);
                                }
                        );
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
                    boolean isStd = STDOUT.equals(outputPath);
                    if (isStd || outputPathToContext.containsKey(outputPath)) {
                        Map<String, String> contextToPath = new HashMap<>();
                        contextToPath.put(
                                outputPathToContext.get(outputPath),
                                outputPath + METADATA_FILE
                        );
                        contextToPath.put(
                                UNKNOWN_CONTEXT,
                                outputPath + UNKNOWN_CONTEXT_METADATA_FILE
                        );
                        contextToPath.forEach(
                                (ctx, path) -> {
                                    try {
                                        String content = JSONUtils.writeAsString(
                                                convertMetadata(ctx)
                                        );
                                        if (isStd)
                                            System.out.println(content);
                                        else
                                            IOUtils.writeString(path, content, false);
                                        logger.debug("Metadata is flushed for log: {}, context: {}", outputPath, ctx);
                                    } catch (Throwable e) {
                                        logger.error("Write metadata of context " + ctx + " to " + path + " is failed.");
                                    }
                                    EventListenerMgr.fireEvent(
                                            new DestInvokeMetadataFlushedEvent(path)
                                    );
                                }
                        );
                    }
                }
        );
    }

    private Map<Class<?>, Map<DestInvoke, Integer>> getClassToInvokeToId(String context) {
        return Optional.ofNullable(
                contextToClassToInvokeToId.get(context)
        ).orElse(
                Collections.emptyMap()
        );
    }

    private Map<String, Map<String, Integer>> convertMetadata(String context) {
        Map<String, Map<String, Integer>> rsMap = new HashMap<>();
        getClassToInvokeToId(context).forEach(
                (clazz, invokeToId) -> {
                    Map<String, Integer> invokeNameToId = rsMap.computeIfAbsent(
                            clazz.getName(),
                            key -> new HashMap<>()
                    );
                    invokeToId.forEach(
                            (invoke, id) -> invokeNameToId.put(
                                    invoke.getName() + invoke.getDescriptor(),
                                    id
                            )
                    );
                }
        );
        return rsMap;
    }

    public Object run(OpFunc func) {
        return lo.syncValue(
                lock -> func.run(contextToClassToInvokeToId)
        );
    }

    public void reset() {
        lo.sync(
                lock -> {
                    contextToClassToInvokeToId.clear();
                    outputPathToContext.clear();
                }
        );
    }

    @Override
    public void onStartup(Object[] args) {
        EventListenerMgr.reg(LogFlushedEvent.class, this);
        EventListenerMgr.reg(ResetEvent.class, this);
    }

    @Override
    public void onShutdown() {

    }

    public interface OpFunc {
        Object run(Map<String, Map<Class<?>, Map<DestInvoke, Integer>>> contextToClassToInvokeToId);
    }
}
