package agent.common.args.parse;

public class SingleOptParser extends AbstractOptParser {
    public SingleOptParser(OptConfig... optConfigs) {
        super(optConfigs);
    }

    @Override
    Object getValue(String arg, ArgList argList, OptConfig optConfig) {
        return true;
    }
}
