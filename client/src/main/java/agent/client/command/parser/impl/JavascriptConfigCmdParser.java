package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.StringCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_JS_CONFIG;

public class JavascriptConfigCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "JS_CONFIG_FILE",
                        "File contains js functions used in js transformation."
                )
        );
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        String script = Utils.wrapToRtError(
                () -> IOUtils.readToString(
                        FileUtils.getAbsolutePath(
                                params.getArgs()[0]
                        )
                )
        );
        return new StringCommand(CMD_JS_CONFIG, script);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"js-config", "jsc"};
    }

    @Override
    public String getDesc() {
        return "Config functions used in javascript transformation.";
    }
}
