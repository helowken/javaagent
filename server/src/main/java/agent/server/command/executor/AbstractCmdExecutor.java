package agent.server.command.executor;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.message.command.Command;
import agent.common.message.command.CommandExecutor;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;

import java.util.Optional;

public abstract class AbstractCmdExecutor implements CommandExecutor {
    private static final Logger logger = Logger.getLogger(AbstractCmdExecutor.class);

    @Override
    public ExecResult exec(Command cmd) {
        try {
            return Optional.ofNullable(doExec(cmd))
                    .orElse(DefaultExecResult.toSuccess(cmd.getType()));
        } catch (Exception e) {
            logger.error("Execute command {} failed.", e, getClass().getName());
            return handleError(e, cmd);
        }
    }

    private ExecResult handleError(Exception e, Command cmd) {
        return DefaultExecResult.toRuntimeError(Utils.getMergedErrorMessage(e));
    }

    abstract ExecResult doExec(Command cmd) throws Exception;
}
