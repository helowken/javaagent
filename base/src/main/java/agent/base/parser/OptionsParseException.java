package agent.base.parser;

public class OptionsParseException extends RuntimeException {
    private final String usageMsg;

    public OptionsParseException(String errMsg, String usageMsg) {
        super(errMsg);
        this.usageMsg = usageMsg;
    }

    public String getUsageMsg() {
        return usageMsg;
    }
}
