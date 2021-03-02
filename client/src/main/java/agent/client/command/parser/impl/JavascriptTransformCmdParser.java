package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.client.command.parser.ClientCmdHelpUtils;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.help.HelpArg;
import agent.common.config.TransformerConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavascriptTransformCmdParser extends AbstractTransformCmdParser {
    @Override
    protected List<HelpArg> createHelpArgList() {
        return Arrays.asList(
                ClientCmdHelpUtils.getTransformerIdHelpArg(),
                new HelpArg(
                        "JS_FILE",
                        "Javascript file used for transformation."
                )
        );
    }

    @Override
    void setConfig(TransformerConfig transformerConfig, CmdParams params) {
        String script = Utils.wrapToRtError(
                () -> IOUtils.readToString(
                        FileUtils.getAbsolutePath(
                                params.getArgs()[1],
                                true
                        )
                )
        );
        transformerConfig.setConfig(
                Collections.singletonMap("script", script)
        );
    }

    @Override
    String getTransformerKey() {
        return "@javascript";
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"js-transform", "jst"};
    }

    @Override
    public String getDesc() {
        return "Use javascript to transform methods and constructors.";
    }
}
