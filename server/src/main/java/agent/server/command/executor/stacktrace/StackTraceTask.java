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

import java.util.*;

import static agent.server.command.executor.stacktrace.StackTraceUtils.convertStackTraceToTree;

public class StackTraceTask implements ScheduleTask {
    private static final StructContext context = new StructContext();
    private static final Logger logger = Logger.getLogger(StackTraceTask.class);
    private static final String SEP = ":";
    private final StackTraceConfig config;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private Tree<StackTraceCountItem> tree = new Tree<>();
    private int id = 0;
    private final AgentFilter<String> threadFilter;
    private final AgentFilter<String> stackFilter;
    private final AgentFilter<String> elementFilter;
    private final String logKey = UUID.randomUUID().toString();

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
        LogMgr.regBinary(
                logKey,
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
        LogMgr.flush(logKey);
        writeMetadataToFile();
        logger.debug("task finish end: {}", logKey);
    }

    @Override
    public void postRun() {
        logger.debug("post run start: {}", logKey);
        nameToId.clear();
        tree = null;
        LogMgr.close(logKey);
        logger.debug("post run finish: {}", logKey);
    }

    private void writeMetadataToFile() {
        LogConfig logConfig = LogMgr.getLogConfig(logKey);
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
