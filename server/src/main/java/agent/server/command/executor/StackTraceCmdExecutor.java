package agent.server.command.executor;

import agent.common.config.StackTraceConfig;
import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.command.executor.stacktrace.StackTraceTask;
import agent.server.schedule.ScheduleMgr;

class StackTraceCmdExecutor extends AbstractCmdExecutor {

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
}
