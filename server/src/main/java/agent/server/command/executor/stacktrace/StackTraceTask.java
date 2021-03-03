package agent.server.command.executor.stacktrace;

import agent.base.utils.Logger;
import agent.common.config.StackTraceConfig;
import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.server.schedule.ScheduleTask;
import agent.server.utils.log.LogMgr;

import java.util.Collections;
import java.util.UUID;


public class StackTraceTask implements ScheduleTask {
    private static final StructContext context = new StructContext();
    private static final Logger logger = Logger.getLogger(StackTraceTask.class);
    private final StackTraceConfig config;
    private final String logKey = UUID.randomUUID().toString();
    private final StackTraceGenerator stGen;

    public StackTraceTask(StackTraceConfig config) {
        this.config = config;
        this.stGen = new StackTraceGenerator(config);
    }

    @Override
    public void run() {
        stGen.generateStackTraces();
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
                buf -> Struct.serialize(
                        buf,
                        stGen.getResult(),
                        context
                )
        );
        LogMgr.flush(logKey);
        logger.debug("task finish end: {}", logKey);
    }

    @Override
    public void postRun() {
        logger.debug("post run start: {}", logKey);
        LogMgr.close(logKey);
        logger.debug("post run finish: {}", logKey);
    }

}
