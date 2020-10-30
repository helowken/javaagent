package agent.client.command.parser.impl;

import agent.base.args.parse.BooleanOptParser;
import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.Opts;
import agent.base.help.HelpArg;
import agent.base.utils.FileUtils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.SaveClassOptConfigs;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.SaveClassConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;

import java.util.Collections;
import java.util.List;

import static agent.client.command.parser.CmdHelpUtils.getOutputPathHelpArg;
import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_SAVE_CLASS;

public class SaveClassCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                merge(
                        getFilterOptParsers(),
                        new BooleanOptParser(
                                SaveClassOptConfigs.getSuite()
                        )
                )
        );
    }

    @Override
    Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        SaveClassConfig config = new SaveClassConfig();
        config.setOutputPath(
                FileUtils.getAbsolutePath(
                        params.getArgs()[0]
                )
        );
        config.setWithSubClasses(
                SaveClassOptConfigs.isWithSubClasses(opts)
        );
        config.setWithSubTypes(
                SaveClassOptConfigs.isWithSubTypes(opts)
        );
        config.setClassFilterConfig(
                FilterOptUtils.createTargetConfig(opts)
                        .getClassFilter()
        );
        return new PojoCommand(CMD_SAVE_CLASS, config);
    }

    @Override
    void checkParams(CmdParams params) {
        FilterOptConfigs.checkClassStr(
                params.getOpts()
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                getOutputPathHelpArg()
        );
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"save-class", "sc"};
    }

    @Override
    public String getDesc() {
        return "Save class bytecode to file.";
    }
}
