package agent.client.command.parser.impl;

import agent.base.args.parse.*;
import agent.base.help.HelpArg;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.ResetOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ResetConfig;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;
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
                merge(
                        getFilterOptParsers(),
                        new BooleanOptParser(
                                ResetOptConfigs.getBoolSuite()
                        )
                )
        );
    }

    @Override
    Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        ResetConfig config = new ResetConfig();
        config.setTargetConfig(
                FilterOptUtils.createTargetConfig(opts)
        );
        Set<String> tids = new HashSet<>();
        Collections.addAll(
                tids,
                params.getArgs()
        );
        config.setTids(tids);
        config.setPrune(
                ResetOptConfigs.isPrune(opts)
        );
        config.validate();
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
