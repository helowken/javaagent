package agent.cmdline.command.parser;

import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.exception.TooFewArgsException;
import agent.cmdline.help.HelpArg;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSection;
import agent.cmdline.help.HelpUtils;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCmdParser<P extends CmdParams> implements CommandParser {
    private CmdParamParser<P> paramParser;
    private List<HelpArg> argList;

    protected abstract CmdParamParser<P> createParamParser();

    protected abstract Command createCommand(P params);

    protected List<HelpArg> createHelpArgList() {
        return Collections.emptyList();
    }

    protected HelpInfo[] getHelpOthers() {
        return null;
    }

    protected HelpInfo getHelpUsage(P params) {
        return HelpUtils.getUsage(
                getCmdNames(),
                getParamParser().hasOptConfig(),
                getHelpArgList(),
                getHelpOthers()
        );
    }

    protected void checkParams(P params) throws Exception {
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

    protected synchronized CmdParamParser<P> getParamParser() {
        if (paramParser == null)
            paramParser = createParamParser();
        return paramParser;
    }

    protected P doParse(String[] args) {
        return getParamParser().parse(args);
    }

    protected HelpInfo createHelpInfo(P params) {
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
    public List<CmdItem> parse(String[] args) throws Exception {
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

    protected boolean isHelp(P params) {
        return params.isHelp();
    }
}
