package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.client.args.parse.CmdParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;

public class EchoCmdParser extends AbstractCmdParser {
    @Override
    CmdParamParser createParamParser() {
        return null;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new EchoCommand(
                params.getArgs()[0]
        );
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"echo"};
    }

    @Override
    public String getDesc() {
        return "Echo message for test.";
    }

}
