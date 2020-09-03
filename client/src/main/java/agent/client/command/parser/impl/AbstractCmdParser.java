package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.OptConfig;
import agent.base.help.HelpArg;
import agent.base.help.HelpInfo;
import agent.base.help.HelpSection;
import agent.base.help.HelpUtils;
import agent.client.args.parse.CmdParams;
import agent.client.command.parser.CmdItem;
import agent.client.command.parser.CommandParser;
import agent.client.command.parser.exception.TooFewArgsException;
import agent.common.message.command.Command;

import java.util.List;

abstract class AbstractCmdParser<P extends CmdParams> implements CommandParser {
    private CmdParamParser<P> paramParser;
    private List<HelpArg> argList;

    abstract CmdParamParser<P> createParamParser();

    abstract Command createCommand(P params);

    abstract List<HelpArg> createHelpArgList();

    HelpInfo getHelpUsage(P params) {
        return HelpUtils.getUsage(
                getCmdNames(),
                getParamParser().hasOptConfig(),
                getHelpArgList()
        );
    }

    void checkParams(P params) {
        long requiredCount = getHelpArgList().stream()
                .filter(
                        arg -> !arg.isOptional()
                )
                .count();
        if (params.getArgs().length < requiredCount)
            throw new TooFewArgsException();
    }

    synchronized List<HelpArg> getHelpArgList() {
        if (argList == null)
            argList = createHelpArgList();
        return argList;
    }

    private synchronized CmdParamParser<P> getParamParser() {
        if (paramParser == null)
            paramParser = createParamParser();
        return paramParser;
    }

    private P doParse(String[] args) {
        return getParamParser().parse(args);
    }

    HelpInfo createHelpInfo(P params) {
        HelpSection section = new HelpSection(null, "").add(
                getHelpUsage(params)
        );
        List<OptConfig> optConfigList = getParamParser().getOptConfigList();
        if (!optConfigList.isEmpty())
            section.add(
                    new HelpSection(HelpSection.PADDING_1 + "Command options:\n", "")
                            .add(
                                    HelpUtils.convert(optConfigList)
                            )
            );
        return section;
    }

    @Override
    public CmdItem parse(String[] args) {
        P params = doParse(args);
        if (isHelp(params)) {
            return new CmdItem(
                    createHelpInfo(params)
            );
        } else {
            checkParams(params);
            return new CmdItem(
                    createCommand(params)
            );
        }
    }

    boolean isHelp(P params) {
        return params.isHelp();
    }
}
