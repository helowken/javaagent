package agent.cmdline.exception;

public class ArgMissingException extends RuntimeException {
    public ArgMissingException(String arg) {
        super("Argument <" + arg + "> is missing.");
    }
}
