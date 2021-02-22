package agent.cmdline.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String msg) {
        super(msg);
    }
}
