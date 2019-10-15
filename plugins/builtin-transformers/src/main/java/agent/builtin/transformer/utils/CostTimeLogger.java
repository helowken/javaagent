package agent.builtin.transformer.utils;

import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.utils.JSONUtils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.LogFlushedEvent;
import agent.server.event.impl.ResetClassEvent;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CostTimeLogger implements AgentEventListener {
    public static final String METADATA_FILE = ".metadata";
    private static final Logger logger = Logger.getLogger(CostTimeLogger.class);
    private static final CostTimeLogger instance = new CostTimeLogger();

    private final LockObject methodTypeLock = new LockObject();
    private final Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = new HashMap<>();
    private boolean dirty = false;
    private final AtomicInteger typeCounter = new AtomicInteger(0);
    private final Map<String, Set<String>> outputPathToContexts = new HashMap<>();
    private final ThreadLocal<CostTimeItem> currItemLocal = new ThreadLocal<>();

    public static CostTimeLogger getInstance() {
        return instance;
    }

    private CostTimeLogger() {
        EventListenerMgr.reg(this);
    }

    public int reg(String context, String className, String methodFullName) {
        return methodTypeLock.syncValue(lock ->
                contextToClassToMethodToType.computeIfAbsent(context, contextKey -> new HashMap<>())
                        .computeIfAbsent(className, classKey -> new HashMap<>())
                        .computeIfAbsent(methodFullName, methodKey -> {
                            dirty = true;
                            return typeCounter.getAndIncrement();
                        })
        );
    }

    public void regOutputPath(String context, String outputPath) {
        methodTypeLock.sync(lock -> {
            if (outputPathToContexts.computeIfAbsent(outputPath, key -> new HashSet<>()).add(context))
                dirty = true;
        });
    }

    public void log(String logKey, int type, int costTime) {
        Optional.ofNullable(currItemLocal.get())
                .orElseGet(() -> {
                    CostTimeItem item = new CostTimeItem(
                            BinaryLogItemPool.get(logKey)
                    );
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
            currItem.end();
            LogMgr.logBinary(logKey, currItem.logItem);
            currItemLocal.remove();
//            logger.debug("Current item is committed.");
        }
    }

    public void rollback() {
//        logger.debug("rollback.");
        currItemLocal.remove();
    }

    @Override
    public void onNotify(AgentEvent event) {
        String eventType = event.getType();
        if (eventType.equals(ResetClassEvent.EVENT_TYPE)) {
            handleResetEvent((ResetClassEvent) event);
        } else if (eventType.equals(LogFlushedEvent.EVENT_TYPE)) {
            handleLogFlushedEvent((LogFlushedEvent) event);
        } else
            throw new RuntimeException("Unsupported event type: " + eventType);
    }

    private void handleResetEvent(ResetClassEvent event) {
        methodTypeLock.sync(lock -> {
            if (event.isResetAll()) {
                outputPathToContexts.clear();
                contextToClassToMethodToType.clear();
                dirty = false;
                logger.debug("Clear all.");
            } else {
                String context = event.getTransformContext().context;
                Set<String> delKeys = new HashSet<>();
                outputPathToContexts.forEach((outputPath, contexts) -> {
                    contexts.remove(context);
                    if (contexts.isEmpty())
                        delKeys.add(outputPath);
                });
                delKeys.forEach(outputPathToContexts::remove);
                if (outputPathToContexts.isEmpty())
                    dirty = false;
                logger.debug("After remove context: {}, outputPathToContexts: {}, dirty: {}", context, outputPathToContexts, dirty);
            }
        });
    }

    private void handleLogFlushedEvent(LogFlushedEvent event) {
        String outputPath = event.getOutputPath();
        methodTypeLock.sync(lock -> {
            if (dirty && outputPathToContexts.containsKey(outputPath)) {
                String content = JSONUtils.writeAsString(contextToClassToMethodToType);
                IOUtils.writeString(outputPath + METADATA_FILE, content, false);
                dirty = false;
                logger.debug("Metadata is flushed for log: {}", outputPath);
            }
        });
    }

    @Override
    public boolean accept(AgentEvent event) {
        String eventType = event.getType();
        return eventType.equals(LogFlushedEvent.EVENT_TYPE)
                || eventType.equals(ResetClassEvent.EVENT_TYPE);
    }

    private static class CostTimeItem {
        private final BinaryLogItem logItem;
        private int count = 0;

        private CostTimeItem(BinaryLogItem logItem) {
            this.logItem = logItem;
            this.logItem.markAndPosition(Integer.BYTES);
        }

        void log(int type, int costTime) {
//            logger.debug("Cost time item type: {}, cost time: {}", type, costTime);
            logItem.putInt(type);
            logItem.putInt(costTime);
            ++count;
        }

        void end() {
            logItem.putIntToMark(count);
        }
    }
}
