package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.args.parse.Opts;
import agent.base.help.HelpArg;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.StackTraceOptConfigs;
import agent.client.command.parser.exception.CommandParseException;
import agent.common.config.StackTraceConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.utils.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static agent.client.command.parser.CmdHelpUtils.getOutputPathHelpArg;
import static agent.client.command.parser.CmdHelpUtils.newLogConfig;
import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_STACK_TRACE;

public class StackTraceCmdParser extends AbstractCmdParser<CmdParams> {
    private static final String KEY_PREFIX = "task:";

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                merge(
                        getHelpOptParser(),
                        new KeyValueOptParser(
                                StackTraceOptConfigs.getSuite()
                        )
                )
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                getOutputPathHelpArg()
        );
    }

    @Override
    void checkParams(CmdParams params) {
        super.checkParams(params);
        Opts opts = params.getOpts();
        long delay = StackTraceOptConfigs.getDelayMs(opts);
        long interval = StackTraceOptConfigs.getInterval(opts);
        int count = StackTraceOptConfigs.getRepeatCount(opts);
        long totalTime = StackTraceOptConfigs.getTotalTime(opts);
        if (delay < 0)
            throw new CommandParseException("Invalid delay. Delay must be >= 0.");
        if (interval < 0)
            throw new CommandParseException("Invalid interval. Interval must be > 0.");
        if (count <= 0 && totalTime <= 0)
            throw new CommandParseException("Either count or time must be specified.");
    }

    @Override
    Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        StackTraceConfig config = new StackTraceConfig();
        String taskKey = StackTraceOptConfigs.getKey(opts);
        if (Utils.isBlank(taskKey))
            taskKey = KEY_PREFIX + UUID.randomUUID();
        config.setKey(taskKey);
        config.setDelayMs(
                StackTraceOptConfigs.getDelayMs(opts)
        );
        config.setIntervalMs(
                StackTraceOptConfigs.getInterval(opts)
        );
        config.setRepeatCount(
                StackTraceOptConfigs.getRepeatCount(opts)
        );
        config.setTotalTimeMs(
                StackTraceOptConfigs.getTotalTime(opts)
        );
        config.setLogConfig(
                newLogConfig(
                        params.getArgs()[0]
                )
        );
        return new MapCommand(
                CMD_STACK_TRACE,
                JsonUtils.convert(
                        config,
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
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
