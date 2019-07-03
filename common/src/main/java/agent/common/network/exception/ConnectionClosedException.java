package agent.common.network.exception;

public class ConnectionClosedException extends RuntimeException {
    public ConnectionClosedException(String msg) {
        super(msg);
    }
}
