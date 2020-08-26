package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.ExecResult;
import agent.common.utils.JsonUtils;
import agent.server.transform.TransformMgr;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<ModuleConfig>() {
                }
        );
        return convert(
                TransformMgr.getInstance().transformByConfig(moduleConfig),
                cmd.getType(),
                PREFIX
        );
    }

}
