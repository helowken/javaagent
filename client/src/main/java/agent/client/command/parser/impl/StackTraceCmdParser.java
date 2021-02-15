package agent.client.command.parser.impl;

import agent.base.args.parse.*;
import agent.base.help.HelpArg;
import agent.base.utils.FileUtils;
import agent.base.utils.Utils;
import agent.common.args.parse.FilterOptUtils;
import agent.common.args.parse.StackTraceOptConfigs;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import java.util.Arrays;
import java.util.List;

import static agent.client.command.parser.CmdHelpUtils.getOutputPathHelpArg;
import static agent.client.command.parser.CmdHelpUtils.newLogConfig;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_STACK_TRACE;

public class StackTraceCmdParser extends ScheduleCmdParser {
    @Override
    List<OptParser> getOptParsers() {
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
    List<HelpArg> createHelpArgList() {
        return Arrays.asList(
                new HelpArg(
                        "TASK_KEY",
                        "Task key which is used to stop the task. It must be unique."
                ),
                getOutputPathHelpArg(false)
        );
    }

    @Override
    Command createCommand(CmdParams params) {
        StackTraceConfig config = new StackTraceConfig();
        populateConfig(params, config);
        config.setLogConfig(
                newLogConfig(
                        FileUtils.getAbsolutePath(
                                params.getArgs()[1]
                        )
                )
        );
        Opts opts = params.getOpts();
        String threadExpr = StackTraceOptConfigs.getThreadFilter(opts);
        if (Utils.isNotBlank(threadExpr))
            config.setThreadFilterConfig(
                    FilterOptUtils.newStringFilterConfig(threadExpr)
            );
        String stackExpr = StackTraceOptConfigs.getStackFilter(opts);
        if (Utils.isNotBlank(stackExpr))
            config.setStackFilterConfig(
                    FilterOptUtils.newStringFilterConfig(stackExpr)
            );
        String elementExpr = StackTraceOptConfigs.getElementFilter(opts);
        if (Utils.isNotBlank(elementExpr))
            config.setElementFilterConfig(
                    FilterOptUtils.newStringFilterConfig(elementExpr)
            );
        config.setMerge(
                StackTraceOptConfigs.isMerge(opts)
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
