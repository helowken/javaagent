package agent.cmdline.command.result;

import agent.base.utils.Registry;
import agent.cmdline.command.Command;

public class ExecResultMgr {
    private final Registry<Integer, ExecResultHandler> registry = new Registry<>();

    public ExecResultMgr reg(int cmdType, ExecResultHandler resultHandler) {
        registry.reg(cmdType, resultHandler);
        return this;
    }

    public void handleResult(Command cmd, ExecResult result) {
        registry.get(
                cmd.getType(),
                DefaultExecResultHandler.getInstance()
        ).handle(cmd, result);
    }
}
