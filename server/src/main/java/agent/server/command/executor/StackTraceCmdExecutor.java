package agent.server.command.executor;

import agent.base.utils.Constants;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.struct.impl.*;
import agent.common.utils.MetadataUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.schedule.ScheduleMgr;
import agent.server.schedule.ScheduleTask;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

import java.util.*;

class StackTraceCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(StackTraceCmdExecutor.class);
    private static final StructContext context = new StructContext();
    private static final ThreadLocal<StackTraceTask> taskLocal = new ThreadLocal<>();

    static {
        context.addPojoInfo(
                Thread.class::isAssignableFrom,
                new PojoInfo<>(
                        StackTraceEntity.TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(Long.class, 0, null, Thread::getId),
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        1,
                                        null,
                                        thread -> taskLocal.get().getNameId(
                                                thread.getName()
                                        )
                                )
                        )
                )
        ).addPojoInfo(
                StackTraceElement.class,
                new PojoInfo<>(
                        StackTraceElementEntity.TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        0,
                                        null,
                                        el -> taskLocal.get().getNameId(
                                                el.getClassName()
                                        )
                                ),
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        1,
                                        null,
                                        el -> taskLocal.get().getNameId(
                                                el.getMethodName()
                                        )
                                )
                        )
                )
        );
    }

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        StackTraceConfig config = cmd.getContent();
        config.validate();
        ScheduleMgr.getInstance().exec(
                config,
                new StackTraceTask(config)
        );
        return null;
    }

    private static class StackTraceTask implements ScheduleTask {
        private static final String SEP = ":";
        private final StackTraceConfig config;
        private String logKey;
        private final Map<String, Integer> nameToId = new HashMap<>();
        private int id = 0;
        private final AgentFilter<String> threadFilter;
        private final AgentFilter<String> stackFilter;
        private final AgentFilter<String> elementFilter;

        StackTraceTask(StackTraceConfig config) {
            this.config = config;
            this.threadFilter = FilterUtils.newStringFilter(
                    config.getThreadFilterConfig()
            );
            this.stackFilter = FilterUtils.newStringFilter(
                    config.getStackFilterConfig()
            );
            this.elementFilter = FilterUtils.newStringFilter(
                    config.getElementFilterConfig()
            );
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
            taskLocal.set(this);
            try {
                Object o = getStackTraces();
                LogMgr.logBinary(
                        logKey,
                        buf -> Struct.serialize(buf, o, context)
                );
            } finally {
                taskLocal.remove();
            }
        }

        private Integer getNameId(String name) {
            return nameToId.computeIfAbsent(
                    name,
                    key -> ++id
            );
        }

        private Map<Thread, List<StackTraceElement>> getStackTraces() {
            Map<Thread, StackTraceElement[]> stMap = Thread.getAllStackTraces();
            Map<Thread, List<StackTraceElement>> rsMap = new HashMap<>();
            stMap.forEach(
                    (thread, stEls) -> {
                        String name = thread.getName();
                        if ((name == null || !name.startsWith(Constants.AGENT_THREAD_PREFIX)) &&
                                (threadFilter == null || threadFilter.accept(thread.getName()))) {
                            List<StackTraceElement> els = new ArrayList<>(stEls.length);
                            boolean flag = false;
                            String entry;
                            for (StackTraceElement stEl : stEls) {
                                entry = stEl.getClassName() + SEP + stEl.getMethodName();
                                if (elementFilter == null || elementFilter.accept(entry))
                                    els.add(stEl);
                                if (!flag && (stackFilter == null || stackFilter.accept(entry)))
                                    flag = true;
                            }
                            if (flag && !els.isEmpty())
                                rsMap.put(thread, els);
                        }
                    }
            );
            return rsMap;
        }
    }
}
