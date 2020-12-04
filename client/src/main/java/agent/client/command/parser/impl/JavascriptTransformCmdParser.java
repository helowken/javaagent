package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.common.config.TransformerConfig;

import java.util.Collections;
import java.util.List;

public class JavascriptTransformCmdParser extends AbstractTransformCmdParser {
    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
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
                                params.getArgs()[0]
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
