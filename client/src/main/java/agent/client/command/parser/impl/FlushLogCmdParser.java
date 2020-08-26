package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.client.args.parse.CmdParams;
import agent.common.message.command.Command;

public class FlushLogCmdParser extends AbstractCmdParser {

    @Override
    public String[] getCmdNames() {
        return new String[]{"flush", "fl"};
    }

    @Override
    public String getDesc() {
        return "Flush data in memory to file.";
    }

    @Override
    CmdParamParser createParamParser() {
        return null;
    }

    @Override
    Command createCommand(CmdParams params) {
        return null;
    }
}
