package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.server.transform.impl.ScriptEngineMgr;


class JavascriptConfigCmdExecutor extends AbstractCmdExecutor {
    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        ScriptEngineMgr.javascript().setGlobalBindings(
                cmd.getContent()
        );
        return null;
    }
}
