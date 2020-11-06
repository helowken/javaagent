package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_ECHO;

public class EchoCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new DefaultCommand(
                CMD_ECHO,
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
