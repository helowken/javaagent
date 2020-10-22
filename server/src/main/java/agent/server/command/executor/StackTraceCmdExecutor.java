package agent.server.command.executor;

import agent.base.utils.Logger;
import agent.base.utils.TypeObject;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;
import agent.common.message.result.ExecResult;
import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;
import agent.common.utils.JsonUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.schedule.ScheduleMgr;
import agent.server.schedule.ScheduleTask;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StackTraceCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(StackTraceCmdExecutor.class);

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        StackTraceConfig config = ((PojoCommand) cmd).getPojo();
        config.validate();
        ScheduleMgr.getInstance().exec(
                config,
                new StackTraceTask(config)
        );
        return null;
    }

    private static class StackTraceTask implements ScheduleTask {
        private Map<String, Object> logConfig;
        private String logKey;

        StackTraceTask(StackTraceConfig config) {
            this.logConfig = config.getLogConfig();
        }

        @Override
        public void preRun() {
            logKey = LogMgr.regBinary(
                    logConfig,
                    Collections.emptyMap()
            );
            logConfig = null;
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
            logger.debug("post run finish: {}", logKey);
        }

        @Override
        public void run() {
            MapStruct<String, Object> struct = Structs.newMap();
            Thread.getAllStackTraces().forEach(
                    (thread, sfEls) -> {
                        StackTraceEntity entity = new StackTraceEntity();
                        entity.setThreadId(thread.getId());
                        entity.setThreadName(thread.getName());
                        entity.setStackTraceElements(
                                sfEls == null ?
                                        Collections.emptyList() :
                                        Stream.of(sfEls).map(
                                                sfEl -> {
                                                    StackTraceElementEntity el = new StackTraceElementEntity();
                                                    el.setClassName(sfEl.getClassName());
                                                    el.setMethodName(sfEl.getMethodName());
                                                    return el;
                                                }
                                        ).collect(Collectors.toList())
                        );

                        Map<String, Object> map = JsonUtils.convert(
                                entity,
                                new TypeObject<Map<String, Object>>() {
                                }
                        );
                        struct.putAll(map);
                        BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
                        logItem.putInt(
                                struct.bytesSize()
                        );
                        struct.serialize(logItem);
                        LogMgr.logBinary(logKey, logItem);
                    }
            );
        }
    }
}
