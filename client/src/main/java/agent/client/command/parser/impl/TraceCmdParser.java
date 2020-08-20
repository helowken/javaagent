package agent.client.command.parser.impl;

import agent.base.help.HelpInfo;
import agent.base.help.HelpSingleValue;
import agent.client.args.parse.TransformParams;

public class TraceCmdParser extends AbstractTransformCmdParser {
    @Override
    String getTransformerKey() {
        return "@traceInvoke";
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"trace"};
    }

    @Override
    public String getDesc() {
        return "Trace arguments, return value and exceptions of methods and constructors.";
    }

    @Override
    HelpInfo getHelpUsage(TransformParams params) {
        return new HelpSingleValue(
                "trace [OPTIONS] OUTPUT_PATH"
        );
    }
}
