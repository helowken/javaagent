package agent.builtin.tools.parse;

import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.cmdline.command.runner.CommandRunner;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSection;
import agent.cmdline.help.HelpSingleValue;

import static agent.cmdline.help.HelpSection.PADDING_1;
import static agent.cmdline.help.HelpSection.PADDING_2;

public class ResultHelpCmdParser extends AbstractHelpCmdParser {

    @Override
    protected HelpInfo getFullHelp() {
        return new HelpSection(null, "")
                .add(
                        new HelpSection("Usage:\n", PADDING_2)
                                .add(
                                        new HelpSingleValue(
                                                "jr <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]"
                                        )
                                )
                )
                .add(
                        new HelpSection("Commands:\n", PADDING_1)
                                .add(
                                        CommandRunner.getInstance().getCmdParseMgr().getCmdHelps()
                                )
                )
                .add(
                        new HelpSingleValue("\nType 'jr help <COMMAND>' to get command-specific help.")
                );
    }
}
