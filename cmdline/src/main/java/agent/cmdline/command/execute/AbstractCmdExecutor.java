package agent.cmdline.command.execute;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.cmdline.command.Command;
import agent.cmdline.command.result.DefaultExecResult;
import agent.cmdline.command.result.ExecResult;

import java.util.Optional;

public abstract class AbstractCmdExecutor implements CommandExecutor {
    private static final Logger logger = Logger.getLogger(AbstractCmdExecutor.class);

    @Override
    public ExecResult exec(Command cmd) {
        try {
            return Optional.ofNullable(
                    doExec(cmd)
            ).orElse(
                    DefaultExecResult.toSuccess(
                            cmd.getType()
                    )
            );
        } catch (Exception e) {
            logger.error("Execute command failed.", e);
            return handleError(e, cmd);
        }
    }

    private ExecResult handleError(Exception e, Command cmd) {
        return DefaultExecResult.toRuntimeError(
                Utils.getMergedErrorMessage(e)
        );
    }

    protected abstract ExecResult doExec(Command cmd) throws Exception;
}
