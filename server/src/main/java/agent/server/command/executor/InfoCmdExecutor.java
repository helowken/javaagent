package agent.server.command.executor;

import agent.common.config.InfoQuery;
import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.DefaultExecResult;
import agent.server.transform.impl.InfoMgr;

import static agent.common.message.MessageType.CMD_INFO;

class InfoCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        InfoQuery infoQuery = cmd.getContent();
        infoQuery.validate();
        return DefaultExecResult.toSuccess(
                CMD_INFO,
                null,
                InfoMgr.create(infoQuery)
        );
    }

}
