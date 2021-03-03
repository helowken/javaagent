package agent.cmdline.command.parser;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.CommonOptConfigs;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSingleValue;
import agent.cmdline.help.HelpUtils;

import java.util.List;

public abstract class AbstractHelpCmdParser extends AbstractCmdParser<CmdParams> {

    protected abstract List<CmdItem> parse(String cmd, String[] cmdArgs);

    protected abstract HelpInfo getFullHelp();

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    protected Command createCommand(CmdParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected HelpInfo getHelpUsage(CmdParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getCmdNames() {
        return HelpUtils.getHelpCmdNames();
    }

    @Override
    public String getDesc() {
        return "Print ja help.";
    }

    private HelpInfo getHelpSelf() {
        return new HelpSingleValue("Print help.");
    }

    @Override
    protected HelpInfo createHelpInfo(CmdParams params) {
        if (params.isHelp())
            return getHelpSelf();

        String[] args = params.getArgs();
        if (args.length == 0)
            return getFullHelp();

        String cmdName = args[0];
        if (Utils.isIn(getCmdNames(), cmdName))
            return getHelpSelf();

        List<CmdItem> itemList = parse(
                cmdName,
                new String[]{
                        CommonOptConfigs.getHelpOptName()
                }
        );
        if (itemList.isEmpty())
            throw new RuntimeException("No help info!");
        return itemList.get(0).getHelpInfo();
    }

    @Override
    protected boolean isHelp(CmdParams params) {
        return true;
    }
}
