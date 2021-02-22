package agent.client.command.parser.impl;

import agent.cmdline.args.parse.DefaultParamParser;
import agent.client.args.parse.ScheduleOptConfigs;
import agent.cmdline.args.parse.*;
import agent.cmdline.exception.CommandParseException;
import agent.common.config.AbstractScheduleConfig;

import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.merge;

abstract class ScheduleCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                merge(
                        new KeyValueOptParser(
                                ScheduleOptConfigs.getSuite()
                        ),
                        getOptParsers()
                )
        );
    }

    protected List<OptParser> getOptParsers() {
        return Collections.emptyList();
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
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
        String taskKey = params.getArgs()[0];
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
