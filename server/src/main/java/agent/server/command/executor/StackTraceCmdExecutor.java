package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.common.config.StackTraceScheduleConfig;
import agent.server.command.executor.stacktrace.StackTraceTask;
import agent.server.schedule.ScheduleMgr;

public class StackTraceCmdExecutor extends AbstractCmdExecutor {

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        StackTraceScheduleConfig config = cmd.getContent();
        config.validate();
        ScheduleMgr.getInstance().exec(
                config,
                new StackTraceTask(config)
        );
        return null;
    }
}
