package agent.launcher.client;

import agent.base.args.parse.OptConfig;
import agent.base.help.HelpInfo;
import agent.base.help.HelpKeyValue;
import agent.base.help.HelpSection;
import agent.base.help.HelpSingleValue;
import agent.base.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static agent.base.help.HelpSection.PADDING_1;
import static agent.base.help.HelpSection.PADDING_2;

class ClientHelpUtils {

    static void printVersion() {
        System.out.println("JavaAgent 1.0.0");
    }

    static void printHelp(List<OptConfig> optConfigList, List<HelpInfo> cmdHelps) {
        StringBuilder sb = new StringBuilder();
        List<HelpInfo> helpInfoList = new ArrayList<>();
        new HelpSection(null, "")
                .add(
                        new HelpSection("Usage:", PADDING_2)
                                .add(
                                        new HelpSingleValue(
                                                "ja [--GLOBAL-OPTIONS] <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]"
                                        )
                                )
                )
                .add(
                        new HelpSection("Global Options:", PADDING_2)
                                .invoke(
                                        section -> optConfigList.stream()
                                                .map(
                                                        optConfig -> new HelpKeyValue(
                                                                getOptConfigNames(optConfig),
                                                                optConfig.getDesc()
                                                        )
                                                ).forEach(section::add)
                                )
                )
                .add(
                        new HelpSection("Commands:", PADDING_1)
                                .add(
                                        new HelpSection(null, PADDING_2)
                                                .add(
                                                        new HelpKeyValue(
                                                                "help, ?",
                                                                "Print ja help."
                                                        )
                                                )
                                )
                                .add(cmdHelps)
                )
                .add(
                        new HelpSingleValue("\nType 'ja help <COMMAND>' to get command-specific help.")
                )
                .print(sb);

        System.out.println(sb);
    }

    private static String getOptConfigNames(OptConfig optConfig) {
        String s = optConfig.getFullName();
        String name = optConfig.getName();
        if (Utils.isBlank(s))
            s = name;
        else if (Utils.isNotBlank(name))
            s += ", " + name;
        return s;
    }
}
