package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.cmdline.args.parse.BooleanOptParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.KeyValueOptParser;
import agent.cmdline.args.parse.OptParser;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;
import agent.cmdline.help.HelpArg;
import agent.common.args.parse.StackTraceOptConfigs;
import agent.common.config.StackTraceScheduleConfig;

import java.util.Arrays;
import java.util.List;

import static agent.client.command.parser.ClientCmdHelpUtils.getOutputPathHelpArg;
import static agent.client.command.parser.ClientCmdHelpUtils.newLogConfig;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_STACK_TRACE;

public class StackTraceCmdParser extends ScheduleCmdParser {
    @Override
    protected List<OptParser> getOptParsers() {
        return merge(
                new KeyValueOptParser(
                        StackTraceOptConfigs.getKvSuite()
                ),
                new BooleanOptParser(
                        StackTraceOptConfigs.getBoolSuite()
                )
        );
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Arrays.asList(
                new HelpArg(
                        "TASK_KEY",
                        "Task key which is used to stop the task. It must be unique."
                ),
                getOutputPathHelpArg(false)
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        StackTraceScheduleConfig config = new StackTraceScheduleConfig();
        populateConfig(params, config);
        config.setLogConfig(
                newLogConfig(
                        FileUtils.getAbsolutePath(
                                params.getArgs()[1],
                                false
                        )
                )
        );
        config.setStackTraceConfig(
                StackTraceOptConfigs.getConfig(
                        params.getOpts()
                )
        );
        config.validate();
        return new DefaultCommand(CMD_STACK_TRACE, config);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"stack-trace", "st"};
    }

    @Override
    public String getDesc() {
        return "Dump all threads' stack trace periodically.";
    }
}
