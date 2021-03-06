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
    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT,
                    "Config the output fields."
            ),
            new OptConfig(
                    "-s",
                    "--content-size",
                    KEY_CONTENT_SIZE,
                    "The max size of each display string.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-hn",
                    "--head-number",
                    KEY_HEAD_NUMBER,
                    "Display num traces from head.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-tn",
                    "--tail-number",
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
