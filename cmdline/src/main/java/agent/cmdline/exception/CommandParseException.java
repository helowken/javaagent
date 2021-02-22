package agent.cmdline.exception;

public class CommandParseException extends RuntimeException {
    public CommandParseException(String msg) {
        super(msg);
    }
}
