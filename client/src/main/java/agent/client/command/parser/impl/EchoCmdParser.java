package agent.client.command.parser.impl;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpArg;
import agent.cmdline.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_ECHO;

public class EchoCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    protected Command createCommand(CmdParams params) {
        return new DefaultCommand(
                CMD_ECHO,
                Utils.join(
                        " ",
                        params.getArgs()
                )
        );
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
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
