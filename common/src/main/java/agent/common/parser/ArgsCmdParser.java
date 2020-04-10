package agent.common.parser;

public interface ArgsCmdParser<O extends BasicOptions, P extends BasicParams<O>> {
    P run(String[] args) throws Exception;
}
