package agent.builtin.tools.args.parse;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

import java.util.HashMap;
import java.util.Map;

public class StackTraceResultOptConfigs {
    private static final String KEY_OUTPUT_FORMAT = "OUTPUT_FORMAT";
    private static final String KEY_RATE = "RATE";
    private static final String KEY_NUMS = "NUMS";
    private static final String KEY_DISPLAY_ALL = "DISPLAY_ALL";
    private static final String KEY_SHORT_NAME = "SHORT_NAME";
    private static final String DEFAULT_RATE = "0.01";
    private static final String NUM_SEP = ",";
    private static final String NUM_PLUS = "+";
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT_FORMAT,
                    "Output format."
            ),
            new OptConfig(
                    "-n",
                    "--number",
                    KEY_NUMS,
                    "Include numbers."
            ),
            new OptConfig(
                    "-r",
                    "--rate",
                    KEY_RATE,
                    "Samples rate."
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
                    "-s",
                    "--short-name",
                    KEY_SHORT_NAME,
                    "Show short name for class."
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

    public static Map<Integer, Boolean> getNumMap(Opts opts) {
        String numStr = opts.get(KEY_NUMS);
        if (Utils.isNotBlank(numStr)) {
            Map<Integer, Boolean> rsMap = new HashMap<>();
            String[] nums = numStr.split(NUM_SEP);
            boolean includeDescendants;
            String num;
            for (String n : nums) {
                n = n.trim();
                num = n;
                if (num.endsWith(NUM_PLUS)) {
                    num = num.substring(
                            0,
                            num.length() - 1
                    );
                    includeDescendants = true;
                } else
                    includeDescendants = false;
                if (Utils.isNotBlank(num))
                    rsMap.put(
                            Utils.parseInt(num, "Number"),
                            includeDescendants
                    );
            }
            return rsMap;
        }
        return null;
    }

    public static float getRate(Opts opts) {
        try {
            return Float.parseFloat(
                    opts.getNotNull(KEY_RATE, DEFAULT_RATE)
            );
        } catch (NumberFormatException e) {
            throw new RuntimeException("Rate must be a float.");
        }
    }

    public static boolean isDisplayAll(Opts opts) {
        return opts.getNotNull(KEY_DISPLAY_ALL, false);
    }

    public static boolean isShortName(Opts opts) {
        return opts.getNotNull(KEY_SHORT_NAME, false);
    }
}
