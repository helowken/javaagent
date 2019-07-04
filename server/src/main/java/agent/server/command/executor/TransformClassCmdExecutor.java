package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformClassCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

class TransformClassCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        TransformMgr.getInstance().transformByConfig(((TransformClassCommand) cmd).getConfig());
        return null;
    }
}
