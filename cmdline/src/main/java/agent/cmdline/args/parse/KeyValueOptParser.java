package agent.cmdline.args.parse;

public class KeyValueOptParser extends AbstractOptParser {
    public KeyValueOptParser(Object... vs) {
        super(vs);
    }

    @Override
    Object getValue(String arg, ArgList argList, OptConfig optConfig) {
        String errMsg = "No option value found for: " + arg;
        String valueStr = argList.next(errMsg);
        if (OptConfig.isOpt(valueStr))
            throw new RuntimeException(errMsg);
        return convertValue(arg, valueStr, optConfig);
    }
}
