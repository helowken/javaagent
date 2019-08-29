package agent.server.transform.config.parser.exception;

public class ConfigParseException extends RuntimeException {
    public ConfigParseException(String errMsg, Throwable t) {
        super(errMsg, t);
    }
}
