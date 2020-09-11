package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.ExecResult;
import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;
import agent.common.utils.JsonUtils;
import agent.server.command.entity.StackTraceEntity;
import agent.server.schedule.ScheduleMgr;
import agent.server.schedule.ScheduleTask;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.util.Collections;
import java.util.Map;

public class StackTraceCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        StackTraceConfig config = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<StackTraceConfig>() {
                }
        );
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
        public void end() {
            LogMgr.flushBinary(logKey);
        }

        @Override
        public void postRun() {
            LogMgr.closeBinary(logKey);
        }

        @Override
        public void run() {
            MapStruct<String, Object> struct = Structs.newMap();
            Thread.getAllStackTraces().forEach(
                    (thread, sfEls) -> {
                        StackTraceEntity entity = new StackTraceEntity();
                        entity.setThreadId(thread.getId());
                        entity.setThreadName(thread.getName());
                        entity.setStackTraceElements(sfEls);

                        Map<String, Object> map = JsonUtils.convert(
                                entity,
                                new TypeObject<Map<String, Object>>() {
                                }
                        );
                        struct.putAll(map);
                        BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
                        struct.serialize(logItem);
                        LogMgr.logBinary(logKey, logItem);
                    }
            );
        }
    }
}
