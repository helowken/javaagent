package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.CommandExecMgr;
import agent.cmdline.command.result.ExecResult;

import static agent.common.message.MessageType.*;

public class ServerCmdExecMgr {
    private static final CommandExecMgr mgr = new CommandExecMgr();

    static {
        mgr.reg(CMD_RESET, new ResetCmdExecutor());
        mgr.reg(CMD_TRANSFORM, new TransformCmdExecutor());
        mgr.reg(CMD_FLUSH_LOG, new FlushLogCmdExecutor());
        mgr.reg(CMD_ECHO, new EchoCmdExecutor());
        mgr.reg(CMD_SEARCH, new SearchCmdExecutor());
        mgr.reg(CMD_INFO, new InfoCmdExecutor());
        mgr.reg(CMD_SAVE_CLASS, new SaveClassCmdExecutor());
        mgr.reg(CMD_STACK_TRACE, new StackTraceCmdExecutor());
        mgr.reg(CMD_JS_CONFIG, new JavascriptConfigCmdExecutor());
        mgr.reg(CMD_JS_EXEC, new JavascriptExecCmdExecutor());
    }

    public static ExecResult exec(Command cmd) {
        return mgr.exec(cmd);
    }
}
