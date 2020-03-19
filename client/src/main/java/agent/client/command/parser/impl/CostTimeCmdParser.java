package agent.client.command.parser.impl;

public class CostTimeCmdParser extends AbstractTransformCmdParser {
    @Override
    String getTransformerKey() {
        return "@costTimeStat";
    }

    @Override
    public String getCmdName() {
        return "costTime";
    }
}
