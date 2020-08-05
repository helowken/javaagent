package agent.common.args.parse;

public class KeyValueOptParser extends AbstractOptParser {
    public KeyValueOptParser(OptConfig... optConfigs) {
        super(optConfigs);
    }

    @Override
    Object getValue(String arg, ArgList argList, OptConfig optConfig) {
        String valueStr = argList.next("No option value found for: " + arg);
        return convertValue(arg, valueStr, optConfig);
    }
}
