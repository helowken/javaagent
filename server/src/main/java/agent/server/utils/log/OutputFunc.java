package agent.server.utils.log;

public interface OutputFunc {
    void exec(OutputWriter outputWriter) throws Exception;
}
