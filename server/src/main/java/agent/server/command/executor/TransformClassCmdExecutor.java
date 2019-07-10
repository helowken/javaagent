package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformClassCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

import static agent.common.message.MessageType.CMD_TRANSFORM_CLASS;

class TransformClassCmdExecutor extends AbstractTransformCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        return convert(
                TransformMgr.getInstance().transformByConfig(
                        ((TransformClassCommand) cmd).getConfig()
                ),
                CMD_TRANSFORM_CLASS,
                "Transform class");
    }
}
