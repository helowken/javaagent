package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.client.args.parse.ModuleParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;

import java.util.Map;

public class ResetCmdParser extends AbstractModuleCmdParser<ModuleParams> {
    @Override
    Command newCommand(Map<String, Object> data) {
        return new ResetCommand(data);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"reset", "rs"};
    }

    @Override
    public String getDesc() {
        return "Reset class bytecode.";
    }

    @Override
    CmdParamParser<ModuleParams> createParamParser() {
        return new DefaultCmdParamParser<>(ModuleParams.class);
    }
}
