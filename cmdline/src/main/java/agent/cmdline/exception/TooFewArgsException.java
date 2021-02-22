package agent.cmdline.exception;

public class TooFewArgsException extends CommandParseException {
    public TooFewArgsException() {
        super("Too few arguments");
    }
}
