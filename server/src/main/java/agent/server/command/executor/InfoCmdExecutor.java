package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.InfoQuery;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.utils.JsonUtils;
import agent.server.transform.impl.InfoMgr;

import static agent.common.message.MessageType.CMD_INFO;

@SuppressWarnings("unchecked")
class InfoCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        InfoQuery infoQuery = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<InfoQuery>() {
                }
        );
        return DefaultExecResult.toSuccess(
                CMD_INFO,
                null,
                InfoMgr.create(infoQuery)
        );
    }

}
