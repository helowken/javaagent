package agent.server.transform.impl;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.base.buffer.ByteUtils;
import agent.base.struct.impl.Struct;
import agent.common.utils.MetadataUtils;
import agent.invoke.DestInvoke;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DestInvokeIdRegistry {
    private static final String SEP = "#";
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);

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

    public void outputMetadata(List<String> outputPaths) {
        if (outputPaths.isEmpty())
            return;
        byte[] bs = lo.syncValue(
                lock -> {
                    try {
                        return ByteUtils.getBytes(
                                Struct.serialize(
                                        convertMetadata()
                                )
                        );
                    } catch (Throwable e) {
                        logger.error("Serialize invoke metadata failed.", e);
                        return null;
                    }
                }
        );
        if (bs != null) {
            outputPaths.forEach(
                    outputPath -> {
                        try {
                            String path = MetadataUtils.getMetadataFile(outputPath);
                            IOUtils.writeBytes(path, bs, false);
                            logger.debug("Metadata is flushed for log: {}", outputPath);
                        } catch (Exception e) {
                            logger.error("Write metadata to failed: {}", e, outputPath);
                        } finally {
                            EventListenerMgr.fireEvent(
                                    new DestInvokeMetadataFlushedEvent(outputPath),
                                    true
                            );
                        }
                    }
            );
        }
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
                Utils.parseInt(ts[1], "Class idx"),
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
                }
        );
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
