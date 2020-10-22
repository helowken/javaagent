package agent.server.command.executor;

import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = ((PojoCommand) cmd).getPojo();
        return convert(
                TransformMgr.getInstance().transformByConfig(moduleConfig),
                cmd.getType(),
                PREFIX
        );
    }

}
