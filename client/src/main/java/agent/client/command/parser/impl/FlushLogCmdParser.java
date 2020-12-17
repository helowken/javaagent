package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_FLUSH_LOG;

public class FlushLogCmdParser extends AbstractCmdParser<CmdParams> {

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
    CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new DefaultCommand(
                CMD_FLUSH_LOG,
                params.hasArgs() ? params.getArgs()[0] : null
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "TID",
                        "Transformer ID.",
                        true
                )
        );
    }

}
