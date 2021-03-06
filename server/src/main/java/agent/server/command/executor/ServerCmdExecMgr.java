package agent.server.command.executor;

import agent.cmdline.command.Command;
import agent.cmdline.command.execute.CommandExecMgr;
import agent.cmdline.command.result.ExecResult;

import static agent.common.message.MessageType.*;

public class ServerCmdExecMgr {
    private static final CommandExecMgr mgr = new CommandExecMgr();

    static {
        mgr.reg(
                new ResetCmdExecutor(),
                CMD_RESET
        ).reg(
                new TransformCmdExecutor(),
                CMD_TRANSFORM
        ).reg(
                new FlushLogCmdExecutor(),
                CMD_FLUSH_LOG
        ).reg(
                new EchoCmdExecutor(),
                CMD_ECHO
        ).reg(
                new SearchCmdExecutor(),
                CMD_SEARCH
        ).reg(
                new InfoCmdExecutor(),
                CMD_INFO
        ).reg(
                new SaveClassCmdExecutor(),
                CMD_SAVE_CLASS
        ).reg(
                new StackTraceCmdExecutor(),
                CMD_STACK_TRACE
        ).reg(
                new JavascriptConfigCmdExecutor(),
                CMD_JS_CONFIG
        ).reg(
                new JavascriptExecCmdExecutor(),
                CMD_JS_EXEC
        );
    }

    public static ExecResult exec(Command cmd) {
        return mgr.exec(cmd);
    }
}
