package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.client.args.parse.JavascriptExecOptConfigs;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;
import agent.cmdline.exception.CommandParseException;
import agent.cmdline.help.HelpArg;
import agent.common.config.JsExec;

import java.util.Collections;
import java.util.List;

import static agent.common.message.MessageType.CMD_JS_EXEC;

public class JavascriptExecCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        JavascriptExecOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
        super.checkParams(params);
        String filePath = JavascriptExecOptConfigs.getFile(
                params.getOpts()
        );
        if (filePath != null)
            FileUtils.getAbsolutePath(filePath, true);
        else if (params.getArgs().length == 0)
            throw new CommandParseException("Script or file must be specified.");
    }

    @Override
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        StringBuilder sb = new StringBuilder();
        String filePath = JavascriptExecOptConfigs.getFile(opts);
        if (filePath != null)
            Utils.wrapToRtError(
                    () -> sb.append(
                            IOUtils.readToString(
                                    FileUtils.getAbsolutePath(filePath, true)
                            )
                    ).append("\n\n")
            );
        if (params.hasArgs()) {
            for (String arg : params.getArgs()) {
                sb.append(arg).append("\n");
            }
        }
        JsExec jse = new JsExec();
        jse.setSessionId(
                JavascriptExecOptConfigs.getSessionId(opts)
        );
        jse.setScript(
                sb.toString()
        );
        return new DefaultCommand(CMD_JS_EXEC, jse);
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "SCRIPT",
                        "Javascript to be executed.",
                        true
                )
        );
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"js-exec", "jse"};
    }

    @Override
    public String getDesc() {
        return "Execute javascript.";
    }
}
