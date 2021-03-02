package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.cmdline.command.result.DefaultExecResult;

class EchoCmdExecutor extends AbstractCmdExecutor {
    @Override
    protected ExecResult doExec(Command cmd) {
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                cmd.getContent()
        );
    }
}
