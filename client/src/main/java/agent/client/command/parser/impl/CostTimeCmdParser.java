package agent.client.command.parser.impl;

public class CostTimeCmdParser extends AbstractTransformCmdParser {
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
