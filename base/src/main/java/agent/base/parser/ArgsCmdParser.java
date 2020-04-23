package agent.base.parser;

public interface ArgsCmdParser<O, P extends BasicParams<O>> {
    P run(String[] args) throws Exception;
}
