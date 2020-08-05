package agent.client.command.parser.impl;

import agent.base.parser.BasicParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;
import agent.common.parser.ChainFilterOptions;

import java.util.Map;

public class ResetCmdParser extends AbstractModuleCmdParser<ChainFilterOptions, BasicParams<ChainFilterOptions>> {

    @Override
    protected BasicParams<ChainFilterOptions> createParams() {
        return new BasicParams<>();
    }

    @Override
    protected ChainFilterOptions createOptions() {
        return new ChainFilterOptions();
    }

    @Override
    Command createCommand(Map<String, Object> data) {
        return new ResetCommand(data);
    }

    @Override
    protected String getMsgFile() {
        return "reset.txt";
    }

    @Override
    public String getCmdName() {
        return "reset";
    }
}
