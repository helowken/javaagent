package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.help.HelpArg;
import agent.base.utils.Utils;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;

import java.util.Collections;
import java.util.List;

public class EchoCmdParser extends AbstractCmdParser {
    @Override
    CmdParamParser createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new EchoCommand(
                Utils.join(
                        " ",
                        params.getArgs()
                )
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg("MESSAGE", "Echo message.")
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
