package agent.client.command.parser.impl;

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
}
