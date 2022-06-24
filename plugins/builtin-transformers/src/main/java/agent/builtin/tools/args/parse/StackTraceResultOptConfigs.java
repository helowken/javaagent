package agent.builtin.tools.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

import java.util.Map;

public class StackTraceResultOptConfigs {
    private static final String KEY_OUTPUT_FORMAT = "OUTPUT_FORMAT";
    private static final String KEY_RATE = "RATE";
    private static final String KEY_NUMS = "NUMS";
    private static final String KEY_DISPLAY_ALL = "DISPLAY_ALL";
    private static final String KEY_RATE_AFTER_FILTER = "KEY_RATE_AFTER_FILTER";
    private static final String DEFAULT_RATE = "0.01";

    public static final String OUTPUT_CONSUMED_TIME_CONFIG = "config";
    public static final String OUTPUT_CONSUMED_TIME_TREE = "tree";
    public static final String OUTPUT_FLAME_GRAPH = "fg";

    private static final String indent = "- ";
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT_FORMAT,
                    "Output format.\n" +
                            indent + OUTPUT_CONSUMED_TIME_CONFIG + ": Output result as filters which can be used in \"ja ct(consumed-time)\".\n" +
                            indent + OUTPUT_CONSUMED_TIME_TREE + ":   Output result as trees.\n" +
                            indent + OUTPUT_FLAME_GRAPH + ":     Output result as flame graph data."
            ),
            new OptConfig(
                    "-n",
                    "--number",
                    KEY_NUMS,
                    "Use number with expr to tailor the result.\n" +
                            "Items are separated by ','.\n" +
                            indent + "nr:  Just include this node.\n" +
                            indent + "+nr: Include this node and its descendants.\n" +
                            indent + "nr+: Include descendants of this node.\n" +
                            indent + "^nr: Just exclude this node.\n" +
                            indent + "-nr: Exclude this node and its descendants.\n" +
                            indent + "nr-: Exclude descendants of this node\n" +
                            "Example: -n '+1,^2,-3,4-,5+' "
            ),
            new OptConfig(
                    "-r",
                    "--rate",
                    KEY_RATE,
                    "Filter nodes which sample rate >= this value."
            )
    );
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-d",
                    "--display-all",
                    KEY_DISPLAY_ALL,
                    "Display info on all nodes."
            ),
            new OptConfig(
                    "-R",
                    "--rate-after-filter",
                    KEY_RATE_AFTER_FILTER,
                    "Calculate sample rate after filtering."
            )
    );

    public static OptConfigSuite getKvSuite() {
        return kvSuite;
    }

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static String getOutputFormat(Opts opts) {
        return opts.get(KEY_OUTPUT_FORMAT);
    }

    public static Map<Integer, Integer> getNumMap(Opts opts) {
        return NumConfig.getNumMap(
                opts.get(KEY_NUMS)
        );
    }

    public static float getRate(Opts opts) {
        int scale = 1;
        String rateStr = opts.getNotNull(KEY_RATE, DEFAULT_RATE);
        if (rateStr.endsWith("%")) {
            rateStr = rateStr.substring(
                    0,
                    rateStr.length() - 1
            );
            scale = 100;
        }
        try {
            return Float.parseFloat(rateStr) / scale;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Rate must be a float.");
        }
    }

    public static boolean isDisplayAll(Opts opts) {
        return opts.getNotNull(KEY_DISPLAY_ALL, false);
    }

    public static boolean isRateAfterFilter(Opts opts) {
        return opts.getNotNull(KEY_RATE_AFTER_FILTER, false);
    }
}
