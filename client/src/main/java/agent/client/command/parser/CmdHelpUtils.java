package agent.client.command.parser;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.help.HelpArg;
import agent.cmdline.help.HelpSection;
import agent.cmdline.help.HelpSingleValue;
import agent.cmdline.help.HelpUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static agent.cmdline.help.HelpSection.PADDING_1;
import static agent.cmdline.help.HelpSection.PADDING_2;

public class CmdHelpUtils {
    private static List<OptConfig> globalOptConfigList;

    public static void setGlobalOptConfigList(List<OptConfig> optConfigs) {
        globalOptConfigList = optConfigs;
    }

    public static HelpSection getHelp() {
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
                                ClientCommandParserMgr.getInstance().getCmdHelps()
                        )
        ).add(
                new HelpSingleValue("\nType 'ja help <COMMAND>' to get command-specific help.")
        );
    }

    public static HelpArg getTransformerIdHelpArg() {
        return new HelpArg(
                "TID",
                "Unique id to identify transformer."
        );
    }

    public static HelpArg getOutputPathHelpArg(boolean isOptional) {
        return new HelpArg(
                "OUTPUT_PATH",
                "File path used to store data.",
                isOptional
        );
    }

    public static Map<String, Object> newLogConfig(String outputPath) {
        return Collections.singletonMap(
                "outputPath",
                outputPath
        );
    }
}
