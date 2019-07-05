package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.FlushLogCommand;
import agent.common.message.result.ExecResult;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;

class FlushLogCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        String outputPath = ((FlushLogCommand) cmd).getOutputPath();
        EventListenerMgr.fireEvent(new FlushLogEvent(outputPath));
        return null;
    }
}
