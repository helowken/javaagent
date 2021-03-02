package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.DefaultExecResult;
import agent.cmdline.command.result.ExecResult;
import agent.common.config.InfoQuery;
import agent.server.transform.impl.InfoMgr;

import static agent.common.message.MessageType.CMD_INFO;

class InfoCmdExecutor extends AbstractCmdExecutor {
    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        InfoQuery infoQuery = cmd.getContent();
        infoQuery.validate();
        return DefaultExecResult.toSuccess(
                CMD_INFO,
                null,
                InfoMgr.create(infoQuery)
        );
    }

}
