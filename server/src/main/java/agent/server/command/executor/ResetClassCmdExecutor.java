package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetClassCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.ResetMgr;

import static agent.common.message.MessageType.CMD_RESET_CLASS;

class ResetClassCmdExecutor extends AbstractTransformCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ResetClassCommand resetCmd = (ResetClassCommand) cmd;
        return convert(
                ResetMgr.getInstance().resetClasses(
                        resetCmd.getContextExpr(),
                        resetCmd.getClassExprSet()
                ),
                CMD_RESET_CLASS,
                "Reset class");
    }
}
