package agent.cmdline.exception;

public class UnknownArgException extends CommandParseException {
    public UnknownArgException(String arg) {
        super("Unknown argument: " + arg);
    }
}
