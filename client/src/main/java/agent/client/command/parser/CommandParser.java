package agent.client.command.parser;

public interface CommandParser {
    CmdItem parse(String[] args) throws Exception;

    String[] getCmdNames();

    String getDesc();
}
