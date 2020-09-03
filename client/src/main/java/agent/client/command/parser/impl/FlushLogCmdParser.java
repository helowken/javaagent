package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.help.HelpArg;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.FlushLogCommand;

import java.util.Collections;
import java.util.List;

public class FlushLogCmdParser extends AbstractCmdParser {

    @Override
    public String[] getCmdNames() {
        return new String[]{"flush", "fl"};
    }

    @Override
    public String getDesc() {
        return "Flush transformer data from memory to file. Transformer can be specified through OUTPUT_PATH.\n" +
                "If no OUTPUT_PATH specified, all transformer data will be flushed.";
    }

    @Override
    CmdParamParser createParamParser() {
        return DefaultCmdParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        return new FlushLogCommand();
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "OUTPUT_PATH",
                        "The data file specified in transformation.",
                        true
                )
        );
    }

}
