package agent.builtin.tools.result.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;
import agent.base.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class StackTraceResultOptConfigs {
    private static final String KEY_OUTPUT_FORMAT = "OUTPUT_FORMAT";
    private static final String KEY_RATE = "RATE";
    private static final String KEY_NUMS = "NUMS";
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
                    "-ns",
                    "--nums",
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

    static OptConfigSuite getKvSuite() {
        return kvSuite;
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
                            Utils.parseInt(num, "\"" + n + "\n"),
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
}
