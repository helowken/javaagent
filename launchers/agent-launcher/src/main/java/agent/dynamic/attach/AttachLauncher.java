package agent.dynamic.attach;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.cmdline.command.runner.DefaultCommandRunner;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSection;
import agent.jvmti.JvmtiUtils;

import java.util.List;

import static agent.cmdline.help.HelpSection.PADDING_1;
import static agent.dynamic.attach.AttachCmdType.CMD_ATTACH;

public class AttachLauncher {
    private static final DefaultCommandRunner cmdRunner = new DefaultCommandRunner();

    static {
        Logger.setSystemLogger(
                ConsoleLogger.getInstance()
        );
        Logger.setAsync(false);

        cmdRunner.getCmdParseMgr()
                .reg(
                        new AttachCmdParser()
                )
                .reg(
                        new AttachHelpCmdParser()
                );

        cmdRunner.getCmdExecMgr().reg(
                CMD_ATTACH,
                new AttachCmdExecutor()
        );
    }

    public static void main(String[] args) {
        JvmtiUtils.getInstance().loadSelfLibrary();
        cmdRunner.run(AttachCmdParser.CMD, args);
    }


    private static class AttachHelpCmdParser extends AbstractHelpCmdParser {
        @Override
        protected List<CmdItem> parse(String cmd, String[] cmdArgs) {
            return null;
        }

        @Override
        protected HelpInfo getFullHelp() {
            return new HelpSection("Commands:\n", PADDING_1)
                    .add(
                            cmdRunner.getCmdParseMgr().getCmdHelps()
                    );
        }
    }
}
