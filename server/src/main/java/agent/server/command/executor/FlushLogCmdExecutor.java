package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.server.utils.log.LogMgr;

class FlushLogCmdExecutor extends AbstractCmdExecutor {
    @Override
    protected ExecResult doExec(Command cmd) {
        LogMgr.flush(
                cmd.getContent()
        );
        return null;
    }
}
