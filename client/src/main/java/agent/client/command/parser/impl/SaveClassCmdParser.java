package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.Opts;
import agent.base.help.HelpArg;
import agent.base.utils.TypeObject;
import agent.client.args.parse.CmdParams;
import agent.client.args.parse.SaveClassOptConfigs;
import agent.client.args.parse.SaveClassParamParser;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.SaveClassConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.utils.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static agent.common.message.MessageType.CMD_SAVE_CLASS;

public class SaveClassCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new SaveClassParamParser();
    }

    @Override
    Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        SaveClassConfig config = new SaveClassConfig();
        config.setOutputPath(
                params.getArgs()[0]
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
        return new MapCommand(
                CMD_SAVE_CLASS,
                JsonUtils.convert(
                        config,
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "OUTPUT_PATH",
                        "File path used to store data."
                )
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
