package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.DefaultExecResult;

class EchoCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                cmd.getContent()
        );
    }
}
