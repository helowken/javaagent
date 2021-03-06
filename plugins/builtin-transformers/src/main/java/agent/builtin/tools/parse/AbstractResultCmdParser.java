package agent.builtin.tools.parse;

import agent.base.utils.FileUtils;
import agent.builtin.tools.config.AbstractResultConfig;
import agent.cmdline.args.parse.CmdParams;
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

    void populateConfig(CmdParams cmdParams, AbstractResultConfig config) {
        config.setInputPath(
                FileUtils.getAbsolutePath(
                        cmdParams.getArgs()[0],
                        true
                )
        );
    }

}
