package agent.builtin.tools;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.builtin.tools.execute.ConsumedTimeResultCmdExecutor;
import agent.builtin.tools.execute.StackTraceResultCmdExecutor;
import agent.builtin.tools.execute.TraceResultCmdExecutor;
import agent.builtin.tools.parse.ConsumedTimeResultCmdParser;
import agent.builtin.tools.parse.ResultHelpCmdParser;
import agent.builtin.tools.parse.StackTraceResultCmdParser;
import agent.builtin.tools.parse.TraceResultCmdParser;
import agent.cmdline.command.runner.CommandRunner;
import agent.cmdline.help.HelpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.builtin.tools.ResultCmdType.*;

public class ResultLauncher {
    static {
        String logPath = "/tmp/jr-" + SystemConfig.getUserName() + ".log";
        Logger.setAsync(false);
        Logger.init(logPath, "DEBUG");

        CommandRunner.getInstance()
                .regParse(
                        new ResultHelpCmdParser()
                )
                .regParse(
                        "Result Print",
                        new TraceResultCmdParser(),
                        new ConsumedTimeResultCmdParser(),
                        new StackTraceResultCmdParser()
                )
                .regExec(
                        new TraceResultCmdExecutor(),
                        CMD_TRACE_RESULT
                )
                .regExec(
                        new ConsumedTimeResultCmdExecutor(),
                        CMD_CONSUMED_TIME_RESULT
                )
                .regExec(
                        new StackTraceResultCmdExecutor(),
                        CMD_STACK_TRACE_RESULT
                )
                .setCmdNotFoundHandler(
                        e -> ConsoleLogger.getInstance().error(
                                "{}",
                                "Type 'jr help' to get a list of global options and commands."
                        )
                );
    }

    public static void main(String[] args) {
        List<String> argList = new ArrayList<>();
        Collections.addAll(argList, args);
        if (argList.isEmpty())
            argList.add(
                    HelpUtils.getHelpCmdName()
            );
        CommandRunner.getInstance().run(argList);
    }

}
