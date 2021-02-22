package agent.client.command.parser.impl;

import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpArg;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_FLUSH_LOG;

public class FlushLogCmdParser extends ClientAbstractCmdParser<CmdParams> {

    @Override
    public String[] getCmdNames() {
        return new String[]{"flush", "fl"};
    }

    @Override
    public String getDesc() {
        return "Flush data of transformer which is specified by TID to file.\n" +
                "If no TID is specified, all transformers will flush.\n";
    }

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    protected Command createCommand(CmdParams params) {
        return new DefaultCommand(
                CMD_FLUSH_LOG,
                params.hasArgs() ? params.getArgs()[0] : null
        );
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "TID",
                        "Transformer ID.",
                        true
                )
        );
    }

}
