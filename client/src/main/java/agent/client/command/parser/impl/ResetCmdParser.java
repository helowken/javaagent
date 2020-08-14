package agent.client.command.parser.impl;

import agent.client.args.parse.DefaultModuleParamParser;
import agent.client.args.parse.ModuleParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;

import java.util.Map;

public class ResetCmdParser extends AbstractModuleCmdParser<ModuleParams> {
    @Override
    Command createCommand(Map<String, Object> data) {
        return new ResetCommand(data);
    }

    @Override
    ModuleParams doParse(String[] args) {
        return DefaultModuleParamParser.getInstance().parse(args);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"reset", "rs"};
    }

    @Override
    public String getDesc() {
        return "Reset class bytecode.";
    }
}
