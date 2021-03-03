package agent.dynamic.attach;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.cmdline.args.parse.CommonOptConfigs;
import agent.cmdline.command.runner.DefaultCommandRunner;
import agent.jvmti.JvmtiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.dynamic.attach.AttachCmdType.CMD_ATTACH;

public class AttachLauncher {
    private static final DefaultCommandRunner cmdRunner = new DefaultCommandRunner();

    static {
        Logger.setSystemLogger(
                ConsoleLogger.getInstance()
        );
        Logger.setAsync(false);

        cmdRunner.getCmdParseMgr().reg(
                new AttachCmdParser()
        );

        cmdRunner.getCmdExecMgr().reg(
                CMD_ATTACH,
                new AttachCmdExecutor()
        );
    }

    public static void main(String[] args) {
        JvmtiUtils.getInstance().loadSelfLibrary();
        List<String> argList = new ArrayList<>();
        argList.add(0, AttachCmdParser.CMD);
        if (args.length == 0)
            argList.add(
                    CommonOptConfigs.getHelpOptName()
            );
        else
            Collections.addAll(argList, args);
        cmdRunner.run(argList);
    }

}
