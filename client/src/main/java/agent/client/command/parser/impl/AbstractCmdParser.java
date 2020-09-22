package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.OptConfig;
import agent.base.help.*;
import agent.common.message.command.CmdItem;
import agent.client.command.parser.CommandParser;
import agent.client.command.parser.exception.TooFewArgsException;
import agent.common.args.parse.ChainFilterOptConfigs;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.message.command.Command;

import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.FILTER_RULE_DESC;

abstract class AbstractCmdParser<P extends CmdParams> implements CommandParser {
    private CmdParamParser<P> paramParser;
    private List<HelpArg> argList;

    abstract CmdParamParser<P> createParamParser();

    abstract Command createCommand(P params);

    List<HelpArg> createHelpArgList() {
        return Collections.emptyList();
    }

    HelpInfo getHelpUsage(P params) {
        HelpInfo[] others = getParamParser().getOptConfigList()
                .stream()
                .anyMatch(
                        optConfig -> FilterOptConfigs.getSuite().contains(optConfig) ||
                                ChainFilterOptConfigs.getSuite().contains(optConfig)
                ) ?
                new HelpInfo[]{
                        new HelpSingleValue(FILTER_RULE_DESC)
                } :
                new HelpInfo[0];

        return HelpUtils.getUsage(
                getCmdNames(),
                getParamParser().hasOptConfig(),
                getHelpArgList(),
                others
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

    private synchronized List<HelpArg> getHelpArgList() {
        if (argList == null)
            argList = createHelpArgList();
        return argList;
    }

    private synchronized CmdParamParser<P> getParamParser() {
        if (paramParser == null)
            paramParser = createParamParser();
        return paramParser;
    }

    P doParse(String[] args) {
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
    public List<CmdItem> parse(String[] args) {
        P params = doParse(args);
        HelpInfo helpInfo = null;
        Command cmd = null;
        if (isHelp(params)) {
            helpInfo = createHelpInfo(params);
        } else {
            checkParams(params);
            cmd = createCommand(params);
        }
        return Collections.singletonList(
                new CmdItem(cmd, helpInfo)
        );
    }

    boolean isHelp(P params) {
        return params.isHelp();
    }
}
