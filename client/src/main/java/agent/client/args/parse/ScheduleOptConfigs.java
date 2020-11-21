package agent.client.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;

public class ScheduleOptConfigs {
    private static final String KEY_DELAY = "DELAY";
    private static final String KEY_INTERVAL = "INTERVAL";
    private static final String KEY_REPEAT_COUNT = "REPEAT_COUNT";
    private static final String KEY_TOTAL_TIME = "TOTAL_TIME";
    private static final long DEFAULT_DELAY = 0;
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-d",
                    "--delay",
                    KEY_DELAY,
                    "Delay in milliseconds before task is to be executed. Default is: " + DEFAULT_DELAY,
                    OptValueType.LONG,
                    false
            ),
            new OptConfig(
                    "-i",
                    "--interval",
                    KEY_INTERVAL,
                    "Time in milliseconds between successive task executions.",
                    OptValueType.LONG,
                    false
            ),
            new OptConfig(
                    "-c",
                    "--count",
                    KEY_REPEAT_COUNT,
                    "Total count for task running.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-t",
                    "--time",
                    KEY_TOTAL_TIME,
                    "Total time for task running.",
                    OptValueType.LONG,
                    false
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static long getDelayMs(Opts opts) {
        return opts.getNotNull(KEY_DELAY, DEFAULT_DELAY);
    }

    public static long getInterval(Opts opts) {
        return opts.get(KEY_INTERVAL);
    }

    public static int getRepeatCount(Opts opts) {
        return opts.getNotNull(KEY_REPEAT_COUNT, 0);
    }

    public static long getTotalTime(Opts opts) {
        return opts.getNotNull(KEY_TOTAL_TIME, 0);
    }

}
