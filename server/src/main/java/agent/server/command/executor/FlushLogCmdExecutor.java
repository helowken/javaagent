package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.FlushLogCommand;
import agent.common.message.result.ExecResult;
import agent.server.utils.IOLogger;

class FlushLogCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        String outputPath = ((FlushLogCommand) cmd).getOutputPath();
        if (outputPath != null)
            IOLogger.getInstance().flush(outputPath);
        else
            IOLogger.getInstance().flushAll();
        return null;
    }
}
