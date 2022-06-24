package agent.builtin.tools.parse;

import agent.base.utils.FileUtils;
import agent.builtin.tools.args.parse.ResultOptConfigs;
import agent.builtin.tools.config.AbstractResultConfig;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.Opts;
import agent.cmdline.command.parser.AbstractCmdParser;
import agent.cmdline.help.HelpArg;

import java.util.Collections;
import java.util.List;

abstract class AbstractResultCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg("FILE", "Data result file.")
        );
    }

    void populateConfig(CmdParams params, AbstractResultConfig config, boolean checkExists) {
        Opts opts = params.getOpts();
        config.setInputPath(
                FileUtils.getAbsolutePath(
                        params.getArgs()[0],
                        checkExists
                )
        );
        config.setShortName(
                ResultOptConfigs.isShortName(opts)
        );
    }

}
