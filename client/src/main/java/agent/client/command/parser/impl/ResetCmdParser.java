package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.help.HelpArg;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;

import java.util.List;
import java.util.Map;

import static agent.common.message.MessageType.CMD_RESET;

public class ResetCmdParser extends AbstractModuleCmdParser<CmdParams> {
    @Override
    Command newCommand(Map<String, Object> data) {
        return new MapCommand(CMD_RESET, data);
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
    CmdParamParser<CmdParams> createParamParser() {
        return DefaultCmdParamParser.DEFAULT;
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return null;
    }
}
