package agent.client.command.parser.impl;

import agent.base.args.parse.*;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.ScheduleOptConfigs;
import agent.client.command.parser.exception.CommandParseException;
import agent.common.config.AbstractScheduleConfig;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;
import static agent.common.args.parse.FilterOptUtils.merge;

abstract class ScheduleCmdParser extends AbstractCmdParser<CmdParams> {
    private static final String KEY_PREFIX = "task:";

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                merge(
                        getHelpOptParser(),
                        new KeyValueOptParser(
                                ScheduleOptConfigs.getSuite()
                        ),
                        getOptParsers()
                )
        );
    }

    List<OptParser> getOptParsers() {
        return Collections.emptyList();
    }

    @Override
    void checkParams(CmdParams params) {
        super.checkParams(params);
        Opts opts = params.getOpts();
        long delay = ScheduleOptConfigs.getDelayMs(opts);
        long interval = ScheduleOptConfigs.getInterval(opts);
        int count = ScheduleOptConfigs.getRepeatCount(opts);
        long totalTime = ScheduleOptConfigs.getTotalTime(opts);
        if (delay < 0)
            throw new CommandParseException("Invalid delay. Delay must be >= 0.");
        if (interval < 0)
            throw new CommandParseException("Invalid interval. Interval must be > 0.");
        if (count <= 0 && totalTime <= 0)
            throw new CommandParseException("Either count or time must be specified.");
    }

    void populateConfig(CmdParams params, AbstractScheduleConfig config) {
        Opts opts = params.getOpts();
        String taskKey = ScheduleOptConfigs.getKey(opts);
        if (Utils.isBlank(taskKey))
            taskKey = KEY_PREFIX + UUID.randomUUID();
        config.setKey(taskKey);
        config.setDelayMs(
                ScheduleOptConfigs.getDelayMs(opts)
        );
        config.setIntervalMs(
                ScheduleOptConfigs.getInterval(opts)
        );
        config.setRepeatCount(
                ScheduleOptConfigs.getRepeatCount(opts)
        );
        config.setTotalTimeMs(
                ScheduleOptConfigs.getTotalTime(opts)
        );
    }

}
