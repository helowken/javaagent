package agent.server.exception;

public class AgentServerException extends RuntimeException {
    public AgentServerException(String msg, Throwable t) {
        super(msg, t);
    }
}
