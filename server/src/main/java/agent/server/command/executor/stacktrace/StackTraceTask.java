package agent.server.command.executor.stacktrace;

import agent.base.utils.Constants;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.common.config.StackTraceConfig;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.Tree;
import agent.common.utils.MetadataUtils;
import agent.server.schedule.ScheduleTask;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.CompoundFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

import java.util.*;

import static agent.server.command.executor.stacktrace.StackTraceUtils.convertStackTraceToTree;

public abstract class StackTraceTask implements ScheduleTask {
    private static final Logger logger = Logger.getLogger(StackTraceTask.class);
    private static final String SEP = ":";
    private static final StructContext context = new StructContext();
    private final StackTraceConfig config;
    private String logKey;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private int id = 0;
    private final AgentFilter<String> threadFilter;
    private final AgentFilter<String> stackFilter;
    private final AgentFilter<String> elementFilter;
    private final Tree<StackTraceCountItem> tree = new Tree<>();

    static {
        context.addPojoInfo(StackTraceCountItem.class, 1);
    }

    public StackTraceTask(StackTraceConfig config) {
        this.config = config;
        this.threadFilter = newThreadFilter(config);
        this.stackFilter = FilterUtils.newStringFilter(
                config.getStackFilterConfig()
        );
        this.elementFilter = FilterUtils.newStringFilter(
                config.getElementFilterConfig()
        );
    }

    private AgentFilter<String> newThreadFilter(StackTraceConfig config) {
        AgentThreadFilter agentThreadFilter = new AgentThreadFilter();
        AgentFilter<String> threadNameFilter = FilterUtils.newStringFilter(
                config.getThreadFilterConfig()
        );
        if (threadNameFilter != null) {
            List<AgentFilter<String>> filters = new ArrayList<>();
            filters.add(agentThreadFilter);
            filters.add(threadNameFilter);
            return new CompoundFilter<>(filters);
        }
        return agentThreadFilter;
    }

    @Override
    public void preRun() {
        logKey = LogMgr.regBinary(
                config.getLogConfig(),
                Collections.emptyMap()
        );
    }

    @Override
    public void finish() {
        logger.debug("task finish start: {}", logKey);
        LogMgr.logBinary(
                logKey,
                buf -> Struct.serialize(buf, tree, context)
        );
        LogMgr.flushBinary(logKey);
        logger.debug("task finish end: {}", logKey);
    }

    @Override
    public void postRun() {
        logger.debug("post run start: {}", logKey);
        LogMgr.closeBinary(logKey);
        writeMetadataToFile();
        nameToId.clear();
        logger.debug("post run finish: {}", logKey);
    }

    private void writeMetadataToFile() {
        LogConfig logConfig = LogMgr.getLogConfig(LoggerType.BINARY, logKey);
        String metadataFile = MetadataUtils.getMetadataFile(
                logConfig.getOutputPath()
        );
        try {
            byte[] bs = ByteUtils.getBytes(
                    Struct.serialize(nameToId)
            );
            IOUtils.writeBytes(metadataFile, bs, false);
        } catch (Exception e) {
            logger.error("Write metadata file failed: {}", e, metadataFile);
        }
    }

    @Override
    public void run() {
        convertStackTraceToTree(
                tree,
                getStackTraces(),
                true,
                thread -> getNameId(
                        thread.getName()
                ),
                el -> getNameId(
                        el.getClassName()
                ),
                el -> getNameId(
                        el.getMethodName()
                )
        );
    }

    private Integer getNameId(String name) {
        return nameToId.computeIfAbsent(
                name,
                key -> ++id
        );
    }

    private Map<Thread, List<StackTraceElement>> getStackTraces() {
        Map<Thread, List<StackTraceElement>> stMap = new HashMap<>();
        Thread.getAllStackTraces().forEach(
                (thread, els) -> stMap.put(thread, Arrays.asList(els))
        );
        return StackTraceUtils.getStackTraces(
                stMap,
                Thread::getName,
                stEl -> stEl.getClassName() + SEP + stEl.getMethodName(),
                threadFilter,
                elementFilter,
                stackFilter
        );
    }

    private static class AgentThreadFilter implements AgentFilter<String> {
        @Override
        public boolean accept(String v) {
            return v == null || !v.startsWith(Constants.AGENT_THREAD_PREFIX);
        }
    }
}
