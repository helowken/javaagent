package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.client.args.parse.DefaultParamParser;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ResetConfig;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import java.util.*;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.message.MessageType.CMD_RESET;

public class ResetCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    public String[] getCmdNames() {
        return new String[]{"reset", "rs"};
    }

    @Override
    public String getDesc() {
        return "Reset class bytecode or remove transformers.";
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterOptParsers()
        );
    }

    @Override
    Command createCommand(CmdParams params) {
        Set<String> tids = new HashSet<>();
        Collections.addAll(
                tids,
                params.getArgs()
        );
        ResetConfig config = new ResetConfig();
        config.setTargetConfig(
                FilterOptUtils.createTargetConfig(
                        params.getOpts()
                )
        );
        config.setTids(tids);
        return new DefaultCommand(CMD_RESET, config);
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "TIDs",
                        "Transformer IDs.",
                        true
                )
        );
    }
}
