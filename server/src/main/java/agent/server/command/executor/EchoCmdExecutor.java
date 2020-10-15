package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.StringCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;

class EchoCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                ((StringCommand) cmd).getContent()
        );
    }
}
