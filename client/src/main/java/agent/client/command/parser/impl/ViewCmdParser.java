package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.client.args.parse.CmdParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;

public class ViewCmdParser extends AbstractCmdParser {

    @Override
    CmdParamParser createParamParser() {
        return null;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new ViewCommand(null);
    }

    @Override
    void checkParams(CmdParams params) {

    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"view"};
    }

    @Override
    public String getDesc() {
        return "Print information.";
    }
}
