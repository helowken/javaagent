package agent.server.command.executor;

import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.command.executor.stacktrace.StackTraceAccumulatedTask;
import agent.server.command.executor.stacktrace.StackTraceRecordTask;
import agent.server.schedule.ScheduleMgr;

class StackTraceCmdExecutor extends AbstractCmdExecutor {

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        StackTraceConfig config = cmd.getContent();
        config.validate();
        ScheduleMgr.getInstance().exec(
                config,
                config.isRecord() ?
                        new StackTraceRecordTask(config) :
                        new StackTraceAccumulatedTask(config)
        );
        return null;
    }
}
