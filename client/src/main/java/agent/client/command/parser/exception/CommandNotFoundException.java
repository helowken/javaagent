package agent.client.command.parser.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String msg) {
        super(msg);
    }
}
