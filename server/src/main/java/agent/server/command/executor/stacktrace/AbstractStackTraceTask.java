package agent.server.command.executor.stacktrace;

import agent.base.utils.Constants;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.common.config.StackTraceConfig;
import agent.common.struct.impl.Struct;
import agent.common.utils.MetadataUtils;
import agent.server.schedule.ScheduleTask;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.CompoundFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

import java.util.*;

abstract class AbstractStackTraceTask implements ScheduleTask {
    private static final Logger logger = Logger.getLogger(StackTraceTask.class);
    private static final String SEP = ":";
    private final StackTraceConfig config;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private int id = 0;
    private final AgentFilter<String> threadFilter;
    private final AgentFilter<String> stackFilter;
    private final AgentFilter<String> elementFilter;
    String logKey;

    AbstractStackTraceTask(StackTraceConfig config) {
        this.config = config;
        this.threadFilter = newThreadFilter(config);
        this.stackFilter = FilterUtils.newStringFilter(
                config.getStackFilterConfig()
        );
        this.elementFilter = FilterUtils.newStringFilter(
                config.getElementFilterConfig()
        );
    }

    abstract void onFinish();

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
        onFinish();
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

    Integer getNameId(String name) {
        return nameToId.computeIfAbsent(
                name,
                key -> ++id
        );
    }

    Map<Thread, List<StackTraceElement>> getStackTraces() {
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
