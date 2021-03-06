package agent.builtin.tools.execute;

import agent.builtin.tools.config.CostTimeResultConfig;
import agent.builtin.tools.execute.handler.CostTimeChainResultHandler;
import agent.builtin.tools.execute.handler.CostTimeInvokeResultHandler;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;

public class CostTimeResultCmdExecutor extends AbstractCmdExecutor {

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        CostTimeResultConfig config = cmd.getContent();
        if (config.isInvoke())
            new CostTimeInvokeResultHandler().process(config);
        else
            new CostTimeChainResultHandler().process(config);
        return null;
    }
}
