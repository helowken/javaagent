package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.utils.log.LogMgr;

class FlushLogCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        LogMgr.flush(
                cmd.getContent()
        );
        return null;
    }
}
