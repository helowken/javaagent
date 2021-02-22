package test.cmdline;

import agent.cmdline.help.HelpKeyValue;
import agent.cmdline.help.HelpSection;
import agent.cmdline.help.HelpSingleValue;
import org.junit.Test;

public class HelpTest {
    @Test
    public void test() {
        StringBuilder sb = new StringBuilder();
        new HelpSection("Usage:", "    ")
                .add(
                        new HelpSingleValue("zypper [--GLOBAL-OPTIONS] <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]")
                )
                .add(
                        new HelpSingleValue("zypper <SUBCOMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]")
                )
                .print(sb);
        new HelpSection("Global Options:", "  ")
                .add(
                        new HelpSection(null, "  ")
                                .add(
                                        new HelpKeyValue("--help, -h", "Help.")
                                )
                                .add(
                                        new HelpKeyValue("--version, -V", "Ouptut the version number")
                                )
                )
                .print(sb);
        new HelpSection("Commands: ", "  ")
                .add(
                        new HelpSection(null, "    ")
                                .add(
                                        new HelpKeyValue("help, ?", "Print zypper help")
                                )
                                .add(
                                        new HelpKeyValue("shell, sh", "Accept multiple commands at once")
                                )
                )
                .add(
                        new HelpSection("Repository Management:", "    ")
                                .add(
                                        new HelpKeyValue("repos, lr", "List all defined repositories")
                                )
                                .add(
                                        new HelpKeyValue("addrepo, ar", "Add a new repository.")
                                )
                )
                .add(
                        new HelpSection("Target Options:", "    ")
                                .add(
                                        new HelpKeyValue("repos, lr", "List all defined repositories")
                                )
                                .add(
                                        new HelpKeyValue("addrepo, ar", "Add a new repository.")
                                )
                )
                .print(sb);
        System.out.println(sb);
    }
}
