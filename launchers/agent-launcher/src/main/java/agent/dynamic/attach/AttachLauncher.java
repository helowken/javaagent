package agent.dynamic.attach;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.cmdline.args.parse.CommonOptConfigs;
import agent.cmdline.command.runner.CommandRunner;
import agent.jvmti.JvmtiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.dynamic.attach.AttachCmdType.CMD_ATTACH;

public class AttachLauncher {
    static {
        Logger.setSystemLogger(
                ConsoleLogger.getInstance()
        );
        Logger.setAsync(false);

        JvmtiUtils.getInstance().loadSelfLibrary();
        CommandRunner.getInstance()
                .regParse(
                        new AttachCmdParser()
                )
                .regExec(
                        new AttachCmdExecutor(),
                        CMD_ATTACH
                );
    }

    public static void main(String[] args) {
        List<String> argList = new ArrayList<>();
        argList.add(0, AttachCmdParser.CMD);
        if (args.length == 0)
            argList.add(
                    CommonOptConfigs.getHelpOptName()
            );
        else
            Collections.addAll(argList, args);

        CommandRunner.getInstance().run(argList);
    }

}
