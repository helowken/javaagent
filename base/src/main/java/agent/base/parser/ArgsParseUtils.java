package agent.base.parser;

public class ArgsParseUtils {
    public static String getErrMsg(Throwable t) {
        String errMsg = t.getMessage();
        if (t instanceof ArgsParseException)
            errMsg += "\n" + ((ArgsParseException) t).getUsageMsg();
        return errMsg;
    }
}
