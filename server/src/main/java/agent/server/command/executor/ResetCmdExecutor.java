package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.ResetMgr;

import static agent.common.message.MessageType.CMD_RESET;

class ResetCmdExecutor extends AbstractTransformCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ResetCommand resetCmd = (ResetCommand) cmd;
        return convert(
                ResetMgr.getInstance().resetClasses(
                        resetCmd.getContextExpr(),
                        resetCmd.getClassExprSet()
                ),
                CMD_RESET,
                "Reset class");
    }
}
