package agent.server.transform.exception;

public class InvalidTransformerConfigException extends RuntimeException {
    public InvalidTransformerConfigException(String msg) {
        super(msg);
    }

    public InvalidTransformerConfigException(String msg, Throwable t) {
        super(msg, t);
    }
}
