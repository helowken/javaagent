package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        return convert(
                TransformMgr.getInstance().transformByConfig(
                        cmd.getContent()
                ),
                cmd.getType(),
                PREFIX
        );
    }

}
