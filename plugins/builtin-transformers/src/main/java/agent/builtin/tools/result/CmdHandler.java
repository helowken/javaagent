package agent.builtin.tools.result;

import agent.common.parser.BasicFilterOptions;
import agent.base.parser.BasicParams;

public interface CmdHandler<O extends BasicFilterOptions, P extends BasicParams<O>> {
    void exec(P params) throws Exception;
}
