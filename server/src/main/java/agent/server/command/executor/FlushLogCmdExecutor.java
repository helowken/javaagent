package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.StringCommand;
import agent.common.message.result.ExecResult;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;

class FlushLogCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        EventListenerMgr.fireEvent(
                new FlushLogEvent(
                        ((StringCommand) cmd).getContent()
                )
        );
        return null;
    }
}
