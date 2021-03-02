package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpArg;
import agent.cmdline.command.DefaultCommand;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_JS_CONFIG;

public class JavascriptConfigCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "JS_CONFIG_FILE",
                        "File contains js functions used in js transformation."
                )
        );
    }

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    protected Command createCommand(CmdParams params) {
        String script = Utils.wrapToRtError(
                () -> IOUtils.readToString(
                        FileUtils.getAbsolutePath(
                                params.getArgs()[0],
                                true
                        )
                )
        );
        return new DefaultCommand(CMD_JS_CONFIG, script);
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
