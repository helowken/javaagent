package agent.server.transform.impl;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.buffer.ByteUtils;
import agent.common.struct.impl.Struct;
import agent.common.utils.MetadataUtils;
import agent.invoke.DestInvoke;
import agent.server.ServerListener;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;
import agent.server.event.impl.LogFlushedEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DestInvokeIdRegistry implements ServerListener, AgentEventListener {
    private static final String SEP = "#";
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final Set<String> outputPaths = new HashSet<>();

    public static DestInvokeIdRegistry getInstance() {
        return instance;
    }

    private DestInvokeIdRegistry() {
    }

    public int reg(DestInvoke destInvoke) {
        return lo.syncValue(
                lock -> classToInvokeToId.computeIfAbsent(
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
                    Map<DestInvoke, Integer> invokeToId = classToInvokeToId.get(
                            destInvoke.getDeclaringClass()
                    );
                    if (invokeToId != null) {
                        Integer id = invokeToId.get(destInvoke);
                        if (id != null)
                            return id;
                    }
                    throw new RuntimeException("No id found for destInvoke: " + destInvoke);
                }
        );
    }

    void regOutputPath(String outputPath) {
        lo.sync(
                lock -> {
                    if (outputPaths.contains(outputPath))
                        throw new RuntimeException("Output path is registered: " + outputPath);
                    outputPaths.add(outputPath);
                }
        );
    }

    @Override
    public void onNotify(AgentEvent event) {
        Class<?> eventType = event.getClass();
        if (eventType.equals(LogFlushedEvent.class)) {
            handleLogFlushedEvent((LogFlushedEvent) event);
        } else
            throw new RuntimeException("Unsupported event type: " + eventType);
    }

    private void handleLogFlushedEvent(LogFlushedEvent event) {
        String outputPath = event.getOutputPath();
        lo.sync(
                lock -> {
                    String path = MetadataUtils.getMetadataFile(outputPath);
                    if (outputPaths.contains(outputPath)) {
                        try {
                            byte[] bs = ByteUtils.getBytes(
                                    Struct.serialize(
                                            convertMetadata()
                                    )
                            );
                            IOUtils.writeBytes(path, bs, false);
                            logger.debug("Metadata is flushed for log: {}", outputPath);
                        } catch (Throwable e) {
                            logger.error("Write metadata to " + path + " is failed.");
                        }
                        EventListenerMgr.fireEvent(
                                new DestInvokeMetadataFlushedEvent(path)
                        );
                    }
                }
        );
    }

    private Map<Integer, String> convertMetadata() {
        Map<Integer, String> rsMap = new HashMap<>();
        Map<Class<?>, Integer> classToIdx = new HashMap<>();
        Map<String, Integer> classNameToIdx = new HashMap<>();
        classToInvokeToId.forEach(
                (clazz, invokeToId) -> invokeToId.forEach(
                        (invoke, id) -> rsMap.computeIfAbsent(
                                id,
                                key -> clazz.getName() + SEP +
                                        getClassNameIdx(classToIdx, classNameToIdx, clazz) + SEP +
                                        invoke.getName() + invoke.getDescriptor()
                        )
                )
        );
        return rsMap;
    }

    private int getClassNameIdx(Map<Class<?>, Integer> classToIdx, Map<String, Integer> classNameToIdx, Class<?> clazz) {
        return classToIdx.computeIfAbsent(
                clazz,
                cls -> {
                    String className = clazz.getName();
                    Integer idx = classNameToIdx.get(className);
                    if (idx == null)
                        idx = 1;
                    else
                        idx += 1;
                    classNameToIdx.put(className, idx);
                    return idx;
                }
        );
    }

    public static InvokeMetadata parse(String s) {
        String[] ts = s.split(SEP);
        if (ts.length != 3)
            throw new RuntimeException("Invalid classInvoke: " + s);
        return new InvokeMetadata(
                ts[0],
                Utils.parseInt(ts[1], "class idx"),
                ts[2]
        );
    }

    public <V> V run(OpFunc<V> func) {
        return lo.syncValue(
                lock -> func.run(classToInvokeToId)
        );
    }

    public void reset() {
        lo.sync(
                lock -> {
                    classToInvokeToId.clear();
                    outputPaths.clear();
                }
        );
    }

    @Override
    public void onStartup(Object[] args) {
        EventListenerMgr.reg(LogFlushedEvent.class, this);
    }

    @Override
    public void onShutdown() {

    }

    public interface OpFunc<V> {
        V run(Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId);
    }

    public static class InvokeMetadata {
        public final String clazz;
        public final int idx;
        public final String invoke;
        private final boolean unknown;

        InvokeMetadata(String clazz, int idx, String invoke) {
            this(clazz, idx, invoke, false);
        }

        InvokeMetadata(String clazz, int idx, String invoke, boolean unknown) {
            this.clazz = clazz;
            this.idx = idx;
            this.invoke = invoke;
            this.unknown = unknown;
        }

        public boolean isUnknown() {
            return unknown;
        }

        public static InvokeMetadata unknown(int invokeId) {
            return new InvokeMetadata(
                    "",
                    1,
                    "invokeId=" + invokeId,
                    true
            );
        }
    }
}
