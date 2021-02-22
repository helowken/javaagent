package agent.cmdline.command;

import java.util.List;

public interface CommandParser {
    List<CmdItem> parse(String[] args) throws Exception;

    String[] getCmdNames();

    String getDesc();
}
