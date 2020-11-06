package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.transform.impl.ScriptEngineMgr;


class JavascriptConfigCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ScriptEngineMgr.javascript().setGlobalBindings(
                cmd.getContent()
        );
        return null;
    }
}
