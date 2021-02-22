package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.client.args.parse.SaveClassOptConfigs;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpArg;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.SaveClassConfig;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.client.command.parser.CmdHelpUtils.getOutputPathHelpArg;
import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_SAVE_CLASS;

public class SaveClassCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
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
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        SaveClassConfig config = new SaveClassConfig();
        config.setOutputPath(
                FileUtils.getAbsolutePath(
                        params.getArgs()[0],
                        false
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
        return new DefaultCommand(CMD_SAVE_CLASS, config);
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
        super.checkParams(params);
        FilterOptConfigs.checkClassFilter(
                params.getOpts()
        );
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                getOutputPathHelpArg(false)
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
