package agent.common.parser;

public interface CmdRunner<O extends BasicOptions, P extends BasicParams<O>> {
    void exec(P params) throws Exception;
}
