package agent.base.parser;

public class ArgsParseException extends RuntimeException {
    private final String usageMsg;

    public ArgsParseException(String errMsg, String usageMsg) {
        super(errMsg);
        this.usageMsg = usageMsg;
    }

    public String getUsageMsg() {
        return usageMsg;
    }
}
