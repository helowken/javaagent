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
import java.util.concurrent.atomic.AtomicInteger;

import static agent.server.utils.log.LogConfig.STDOUT;

public class DestInvokeIdRegistry implements ServerListener, AgentEventListener {
    public static final String UNKNOWN_CONTEXT = "@unknownContext@";
    public static final String METADATA_FILE = ".metadata";
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<String, Map<Class<?>, Map<DestInvoke, Integer>>> contextToClassToInvokeToId = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final Map<String, Set<String>> outputPathToContexts = new HashMap<>();

    public static DestInvokeIdRegistry getInstance() {
        return instance;
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

    public void regOutputPath(String sContext, String outputPath) {
        lo.sync(
                lock -> {
                    outputPathToContexts.computeIfAbsent(
                            outputPath,
                            key -> new HashSet<>()
                    ).add(
                            convertNullToUnknown(sContext)
                    );
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
                        outputPathToContexts.clear();
                        logger.debug("Clear all.");
                    } else {
                        String context = convertNullToUnknown(
                                event.getContext()
                        );
                        Set<String> delKeys = new HashSet<>();
                        outputPathToContexts.forEach((outputPath, ctxs) -> {
                            ctxs.remove(context);
                            if (ctxs.isEmpty())
                                delKeys.add(outputPath);
                        });
                        delKeys.forEach(outputPathToContexts::remove);
                        logger.debug("After remove context: {}, outputPathToContexts: {}", context, outputPathToContexts);
                    }
                }
        );
    }

    private void handleLogFlushedEvent(LogFlushedEvent event) {
        String outputPath = event.getOutputPath();
        lo.sync(
                lock -> {
                    boolean isStd = STDOUT.equals(outputPath);
                    if (isStd || outputPathToContexts.containsKey(outputPath)) {
                        String path = outputPath + METADATA_FILE;
                        for (String ctx : outputPathToContexts.get(outputPath)) {
                            String content = JSONUtils.writeAsString(
                                    convertMetadata(ctx)
                            );
                            if (isStd)
                                System.out.println(content);
                            else {
                                try {
                                    IOUtils.writeString(path, content, false);
                                } catch (Exception e) {
                                    logger.error("Write metadata of context " + ctx + " to " + path + " is failed.");
                                }
                            }
                        }
                        logger.debug("Metadata is flushed for log: {}", outputPath);
                        EventListenerMgr.fireEvent(
                                new DestInvokeMetadataFlushedEvent(path)
                        );
                    }
                }
        );
    }

    private Map<Class<?>, Map<DestInvoke, Integer>> getClassToInvokeToId(String context) {
        return Optional.ofNullable(
                contextToClassToInvokeToId.get(context)
        ).orElseThrow(
                () -> new RuntimeException("Invalid context: " + context)
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

    private Collection<String> getContexts(Collection<String> sContexts) {
        return sContexts == null ?
                contextToClassToInvokeToId.keySet() :
                sContexts;
    }

    public Map<String, Collection<Class<?>>> getClassesOfContext(Collection<String> sContexts) {
        return lo.syncValue(
                lock -> {
                    Map<String, Collection<Class<?>>> rsMap = new HashMap<>();
                    getContexts(sContexts).forEach(
                            sContext -> {
                                String context = convertNullToUnknown(sContext);
                                rsMap.put(
                                        context,
                                        new ArrayList<>(
                                                getClassToInvokeToId(context).keySet()
                                        )
                                );
                            }
                    );
                    return rsMap;
                }
        );
    }

    public Map<Class<?>, Collection<DestInvoke>> getDestInvokesOfClass(Collection<String> sContexts, Collection<Class<?>> classes) {
        Set<Class<?>> includeClasses = classes == null ?
                null :
                new HashSet<>(classes);
        return lo.syncValue(
                lock -> {
                    Map<Class<?>, Collection<DestInvoke>> rsMap = new HashMap<>();
                    getContexts(sContexts).forEach(
                            context -> getClassToInvokeToId(context).forEach(
                                    (clazz, invokeToId) -> {
                                        if (includeClasses == null || includeClasses.contains(clazz)) {
                                            rsMap.put(
                                                    clazz,
                                                    new ArrayList<>(
                                                            invokeToId.keySet()
                                                    )
                                            );
                                        }
                                    }
                            )
                    );
                    return rsMap;
                }
        );
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
