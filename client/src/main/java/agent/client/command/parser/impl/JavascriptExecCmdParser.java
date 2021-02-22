package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.client.args.parse.JavascriptExecOptConfigs;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.KeyValueOptParser;
import agent.cmdline.command.Command;
import agent.common.message.command.DefaultCommand;

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
    }

    @Override
    protected Command createCommand(CmdParams params) {
        StringBuilder sb = new StringBuilder();
        String filePath = JavascriptExecOptConfigs.getFile(
                params.getOpts()
        );
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
        return new DefaultCommand(
                CMD_JS_EXEC,
                sb.toString()
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
