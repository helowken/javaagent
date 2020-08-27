package agent.client.command.parser.exception;

public class TooFewArgsException extends CommandParseException {
    public TooFewArgsException() {
        super("Too few arguments");
    }
}
