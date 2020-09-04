package agent.client.command.parser.impl;

public class TraceCmdParser extends AbstractTransformCmdParser {
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
        return "Printf arguments, return value and errors of methods and constructors.";
    }
}
