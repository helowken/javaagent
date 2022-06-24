package agent.builtin.tools.execute;

import agent.builtin.tools.config.ConsumedTimeResultConfig;
import agent.builtin.tools.execute.handler.ConsumedTimeChainResultHandler;
import agent.builtin.tools.execute.handler.ConsumedTimeInvokeResultHandler;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;

public class ConsumedTimeResultCmdExecutor extends AbstractCmdExecutor {

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        ConsumedTimeResultConfig config = cmd.getContent();
        if (config.isInvoke())
            new ConsumedTimeInvokeResultHandler().process(config);
        else
            new ConsumedTimeChainResultHandler().process(config);
        return null;
    }
}
