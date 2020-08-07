package agent.builtin.tools.result.parse;

import agent.common.args.parse.Opts;

public interface ResultParams {
    String getConfigFile();

    String getInputPath();

    Opts getOpts();
}
