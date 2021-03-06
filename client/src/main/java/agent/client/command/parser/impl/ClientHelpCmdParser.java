package agent.client.command.parser.impl;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.cmdline.command.runner.CommandRunner;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSection;
import agent.cmdline.help.HelpSingleValue;
import agent.cmdline.help.HelpUtils;

import java.util.List;

import static agent.cmdline.help.HelpSection.PADDING_1;
import static agent.cmdline.help.HelpSection.PADDING_2;

public class ClientHelpCmdParser extends AbstractHelpCmdParser {
    private final List<OptConfig> globalOptConfigList;

    public ClientHelpCmdParser(List<OptConfig> globalOptConfigList) {
        this.globalOptConfigList = globalOptConfigList;
    }

    @Override
    protected HelpInfo getFullHelp() {
        HelpSection section = new HelpSection(null, "")
                .add(
                        new HelpSection("Usage:\n", PADDING_2)
                                .add(
                                        new HelpSingleValue(
                                                "ja [--GLOBAL-OPTIONS] <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]"
                                        )
                                )
                );

        if (globalOptConfigList != null && !globalOptConfigList.isEmpty())
            section.add(
                    new HelpSection("Global Options:\n", PADDING_2)
                            .invoke(
                                    sc -> sc.add(
                                            HelpUtils.convert(globalOptConfigList)
                                    )
                            )
            );

        return section.add(
                new HelpSection("Commands:\n", PADDING_1)
                        .add(
                                CommandRunner.getInstance().getCmdParseMgr().getCmdHelps()
                        )
        ).add(
                new HelpSingleValue("\nType 'ja help <COMMAND>' to get command-specific help.")
        );
    }
}
