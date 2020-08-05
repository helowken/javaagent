package agent.server.command.executor;

import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.ResetMgr;
import agent.server.transform.config.parser.ConfigParser;

class ResetCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Reset";

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = ConfigParser.parse(
                ((ResetCommand) cmd).getConfig()
        );
        return convert(
                ResetMgr.getInstance().resetClasses(moduleConfig),
                cmd.getType(),
                PREFIX
        );
    }
}
