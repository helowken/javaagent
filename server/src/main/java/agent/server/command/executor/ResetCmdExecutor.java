package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.ExecResult;
import agent.common.utils.JsonUtils;
import agent.server.transform.ResetMgr;

class ResetCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Reset";

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<ModuleConfig>() {
                }
        );
        return convert(
                ResetMgr.getInstance().resetClasses(null),
                cmd.getType(),
                PREFIX
        );
    }
}
