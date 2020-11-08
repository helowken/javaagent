package agent.server.command.executor;

import agent.base.utils.Constants;
import agent.base.utils.Logger;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.struct.impl.*;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.schedule.ScheduleMgr;
import agent.server.schedule.ScheduleTask;
import agent.server.utils.log.LogMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class StackTraceCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(StackTraceCmdExecutor.class);
    private static final StructContext context = new StructContext();

    static {
        context.addPojoInfo(
                Thread.class::isAssignableFrom,
                new PojoInfo<>(
                        StackTraceEntity.TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(Long.class, 0, null, Thread::getId),
                                new PojoFieldProperty<>(String.class, 1, null, Thread::getName)
                        )
                )
        ).addPojoInfo(
                StackTraceElement.class,
                new PojoInfo<>(
                        StackTraceElementEntity.TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(String.class, 0, null, StackTraceElement::getClassName),
                                new PojoFieldProperty<>(String.class, 1, null, StackTraceElement::getMethodName)
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
            Object o = getStackTraces();
            LogMgr.logBinary(
                    logKey,
                    buf -> Struct.serialize(buf, o, context)
            );
        }

        private Map<Thread, StackTraceElement[]> getStackTraces() {
            Map<Thread, StackTraceElement[]> stMap = Thread.getAllStackTraces();
            Map<Thread, StackTraceElement[]> rsMap = new HashMap<>();
            stMap.forEach(
                    (thread, stEls) -> {
                        String name = thread.getName();
                        if (name == null || !name.startsWith(Constants.THREAD_PREFIX))
                            rsMap.put(thread, stEls);
                    }
            );
            return rsMap;
        }
    }
}
