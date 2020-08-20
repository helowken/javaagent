package agent.client.command.parser;

import agent.base.args.parse.OptConfig;
import agent.base.help.HelpSection;
import agent.base.help.HelpSingleValue;
import agent.base.help.HelpUtils;

import java.util.List;

import static agent.base.help.HelpSection.PADDING_1;
import static agent.base.help.HelpSection.PADDING_2;

public class CmdHelpUtils {
    private static List<OptConfig> optConfigList;

    public static void setOptConfigList(List<OptConfig> optConfigs) {
        optConfigList = optConfigs;
    }

    public static HelpSection getHelp() {
        HelpSection section = new HelpSection(null, "")
                .add(
                        new HelpSection("Usage:", PADDING_2)
                                .add(
                                        new HelpSingleValue(
                                                "ja [--GLOBAL-OPTIONS] <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]"
                                        )
                                )
                );

        if (optConfigList != null && !optConfigList.isEmpty())
            section.add(
                    new HelpSection("Global Options:", PADDING_2)
                            .invoke(
                                    sc -> sc.add(
                                            HelpUtils.convert(optConfigList)
                                    )
                            )
            );

        return section.add(
                new HelpSection("Commands:", PADDING_1)
                        .add(
                                CommandParserMgr.getCmdHelps()
                        )
        ).add(
                new HelpSingleValue("\nType 'ja help <COMMAND>' to get command-specific help.")
        );
    }
}
