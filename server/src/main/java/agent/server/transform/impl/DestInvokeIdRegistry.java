package agent.server.transform.impl;

import agent.base.buffer.ByteUtils;
import agent.base.struct.annotation.PojoClass;
import agent.base.struct.annotation.PojoProperty;
import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.utils.MetadataUtils;
import agent.invoke.DestInvoke;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.DestInvokeMetadataFlushedEvent;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata.POJO_TYPE;

public class DestInvokeIdRegistry {
    private static final Logger logger = Logger.getLogger(DestInvokeIdRegistry.class);
    private static final DestInvokeIdRegistry instance = new DestInvokeIdRegistry();

    private final LockObject lo = new LockObject();
    private final Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final StructContext context = new StructContext();

    public static DestInvokeIdRegistry getInstance() {
        return instance;
    }

    private DestInvokeIdRegistry() {
        context.setPojoCreator(
                type -> {
                    if (type == POJO_TYPE)
                        return new InvokeMetadata();
                    throw new RuntimeException("Unknown pojo type: " + type);
                }
        );
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
                                        convertMetadata(),
                                        context
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

    private Map<Integer, InvokeMetadata> convertMetadata() {
        Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
        classToInvokeToId.forEach(
                (clazz, invokeToId) -> invokeToId.forEach(
                        (invoke, invokeId) -> rsMap.computeIfAbsent(
                                invokeId,
                                key -> new InvokeMetadata(
                                        clazz.getName(),
                                        Utils.identityHashCode(clazz),
                                        invoke.getName() + invoke.getDescriptor(),
                                        false
                                )
                        )
                )
        );
        return rsMap;
    }

    public Map<Integer, InvokeMetadata> parse(byte[] bs) {
        return Struct.deserialize(
                ByteBuffer.wrap(bs),
                context
        );
    }

    public <V> V run(OpFunc<V> func) {
        return lo.syncValue(
                lock -> func.run(classToInvokeToId)
        );
    }

    public void reset() {
        lo.sync(
                lock -> classToInvokeToId.clear()
        );
    }

    public interface OpFunc<V> {
        V run(Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId);
    }

    @PojoClass(type = POJO_TYPE)
    public static class InvokeMetadata {
        static final int POJO_TYPE = 1;
        @PojoProperty(index = 0)
        public String clazz;
        @PojoProperty(index = 1)
        public int cid;
        @PojoProperty(index = 2)
        public String invoke;
        private boolean unknown;

        InvokeMetadata() {
        }

        InvokeMetadata(String clazz, int cid, String invoke, boolean unknown) {
            this.clazz = clazz;
            this.cid = cid;
            this.invoke = invoke;
            this.unknown = unknown;
        }

        public boolean isUnknown() {
            return unknown;
        }

        public static InvokeMetadata unknown(int invokeId) {
            return new InvokeMetadata(
                    "",
                    0,
                    "invokeId=" + invokeId,
                    true
            );
        }
    }
}
