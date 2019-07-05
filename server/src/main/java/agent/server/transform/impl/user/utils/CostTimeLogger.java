package agent.server.transform.impl.user.utils;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryConverterRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CostTimeLogger {
    private static final Logger logger = Logger.getLogger(CostTimeLogger.class);
    private static final CostTimeLogger instance = new CostTimeLogger();

    private final LockObject methodTypeLock = new LockObject();
    private final Map<String, Integer> methodNameToType = new HashMap<>();
    private final AtomicInteger typeCounter = new AtomicInteger(0);
    private final ThreadLocal<CostTimeItem> currItemLocal = new ThreadLocal<>();

    static {
        BinaryConverterRegistry.reg(CostTimeItem.class, v -> {
            CostTimeItem item = (CostTimeItem) v;
            int size = item.typeToCostTime.size() * (Short.BYTES + Integer.BYTES);
            byte[] bs = new byte[size];
            int idx = 0;
            for (Map.Entry<Short, Integer> entry : item.typeToCostTime.entrySet()) {
                idx = ByteUtils.putShort(bs, idx, entry.getKey());
                idx = ByteUtils.putInt(bs, idx, entry.getValue());
            }
            return bs;
        });
    }

    public static CostTimeLogger getInstance() {
        return instance;
    }

    private CostTimeLogger() {
    }

    public short reg(String methodFullName) {
        return methodTypeLock.syncValue(lock ->
                methodNameToType.computeIfAbsent(methodFullName,
                        key -> typeCounter.getAndIncrement()
                )
        ).shortValue();
    }

    public void log(short type, int costTime) {
        Optional.ofNullable(currItemLocal.get())
                .orElseGet(() -> {
                    CostTimeItem item = new CostTimeItem();
                    currItemLocal.set(item);
                    return item;
                })
                .log(type, costTime);
    }

    public void commit(String logKey) {
        CostTimeItem currItem = currItemLocal.get();
        if (currItem == null)
            logger.warn("No cost time item found, but commit is called, log key is: {}", logKey);
        else {
            LogMgr.logBinary(logKey, currItem);
            currItemLocal.remove();
        }
    }

    private static class CostTimeItem {
        private final Map<Short, Integer> typeToCostTime = new HashMap<>();

        void log(short type, int costTime) {
            logger.debug("Cost time item type: {}, cost time: {}", type, costTime);
            typeToCostTime.put(type, costTime);
        }
    }
}
