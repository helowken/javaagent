package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.help.HelpArg;
import agent.common.config.TransformerConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static agent.client.command.parser.CmdHelpUtils.getOutputPathHelpArg;
import static agent.client.command.parser.CmdHelpUtils.getTransformerIdHelpArg;

public abstract class BuiltInTransformCmdParser extends AbstractTransformCmdParser {
    @Override
    protected List<HelpArg> createHelpArgList() {
        return Arrays.asList(
                getTransformerIdHelpArg(),
                getOutputPathHelpArg(true)
        );
    }

    @Override
    void setConfig(TransformerConfig transformerConfig, CmdParams params) {
        if (params.getArgs().length > 1) {
            transformerConfig.setConfig(
                    Collections.singletonMap(
                            "log",
                            Collections.singletonMap(
                                    "outputPath",
                                    FileUtils.getAbsolutePath(
                                            params.getArgs()[1],
                                            false
                                    )
                            )
                    )
            );
        }
    }

    public static class TraceCmdParser extends BuiltInTransformCmdParser {
        @Override
        String getTransformerKey() {
            return "@traceInvoke";
        }

        @Override
        public String[] getCmdNames() {
            return new String[]{"trace", "tr"};
        }

        @Override
        public String getDesc() {
            return "Print arguments, return value and errors of methods and constructors.";
        }
    }

    public static class CostTimeCmdParser extends BuiltInTransformCmdParser {
        @Override
        String getTransformerKey() {
            return "@costTimeStat";
        }

        @Override
        public String[] getCmdNames() {
            return new String[]{"cost-time", "ct"};
        }

        @Override
        public String getDesc() {
            return "Log cost time for methods and constructors.";
        }

    }
}
