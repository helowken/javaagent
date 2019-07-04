package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetClassCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

class ResetClassCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        ResetClassCommand resetCmd = (ResetClassCommand) cmd;
        TransformMgr.getInstance().resetClasses(resetCmd.getContextExpr(), resetCmd.getClassExprSet());
        return null;
    }
}
