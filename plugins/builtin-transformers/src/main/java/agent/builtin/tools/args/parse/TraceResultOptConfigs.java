package agent.builtin.tools.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.OptValueType;
import agent.cmdline.args.parse.Opts;

public class TraceResultOptConfigs {
    private static final String KEY_OUTPUT = "OUTPUT";
    private static final String KEY_CONTENT_SIZE = "CONTENT_SIZE";
    private static final String KEY_HEAD_NUMBER = "HEAD_NUMBER";
    private static final String KEY_TAIL_NUMBER = "TAIL_NUMBER";
    private static final int DEFAULT_CONTENT_SIZE = 1000;

    public static final String DISPLAY_CONSUMED_TIME = "ct";
    public static final String DISPLAY_START_TIME = "st";
    public static final String DISPLAY_ARGS = "args";
    public static final String DISPLAY_RETURN_VALUE = "rv";
    public static final String DISPLAY_ERROR = "err";

    private static final String indent = "- ";
    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT,
                    "Config the output fields, separated by ','.\n" +
                            indent + DISPLAY_CONSUMED_TIME + ":   Consumed time.\n" +
                            indent + DISPLAY_START_TIME + ":   Start time.\n" +
                            indent + DISPLAY_ARGS + ": Arguments.\n" +
                            indent + DISPLAY_RETURN_VALUE + ":   Return value.\n" +
                            indent + DISPLAY_ERROR + ":  Error if raised."
            ),
            new OptConfig(
                    "-z",
                    "--content-size",
                    KEY_CONTENT_SIZE,
                    "The max size of each display string.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-hn",
                    "--head",
                    KEY_HEAD_NUMBER,
                    "Display num traces from head.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-tn",
                    "--tail",
                    KEY_TAIL_NUMBER,
                    "Display num traces from tail.",
                    OptValueType.INT,
                    false
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static int getContentSize(Opts opts) {
        return opts.getNotNull(KEY_CONTENT_SIZE, DEFAULT_CONTENT_SIZE);
    }

    public static String getOutput(Opts opts) {
        return opts.get(KEY_OUTPUT);
    }

    public static int getHeadNumber(Opts opts) {
        return opts.getNotNull(KEY_HEAD_NUMBER, -1);
    }

    public static int getTailNumber(Opts opts) {
        return opts.getNotNull(KEY_TAIL_NUMBER, -1);
    }
}
