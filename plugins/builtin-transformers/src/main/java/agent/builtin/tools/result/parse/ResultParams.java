package agent.builtin.tools.result.parse;

import agent.base.args.parse.Opts;

public interface ResultParams {
    String getConfigFile();

    String getInputPath();

    Opts getOpts();
}
