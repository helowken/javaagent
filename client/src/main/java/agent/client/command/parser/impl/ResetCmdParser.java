package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultParamParser;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ResetConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;

import static agent.common.args.parse.FilterOptUtils.getMatchClassFilterOptParsers;
import static agent.common.message.MessageType.CMD_RESET;

public class ResetCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    public String[] getCmdNames() {
        return new String[]{"reset", "rs"};
    }

    @Override
    public String getDesc() {
        return "Reset class bytecode.";
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getMatchClassFilterOptParsers()
        );
    }

    @Override
    Command createCommand(CmdParams params) {
        ResetConfig config = new ResetConfig();
        config.setTargetConfig(
                FilterOptUtils.createTargetConfig(
                        params.getOpts()
                )
        );
        return new PojoCommand(CMD_RESET, config);
    }
}
