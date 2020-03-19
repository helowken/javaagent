package agent.client.command.parser.impl;

public class TraceCmdParser extends AbstractTransformCmdParser {
    @Override
    String getTransformerKey() {
        return "@traceInvoke";
    }

    @Override
    public String getCmdName() {
        return "trace";
    }
}
