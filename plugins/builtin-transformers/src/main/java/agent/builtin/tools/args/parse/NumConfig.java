package agent.builtin.tools.args.parse;

import agent.base.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class NumConfig {
    private static final String NUM_SEP = ",";
    public static final int NONE = 0;
    public static final int INCLUDE_SELF = 1;
    public static final int INCLUDE_DESCENDANT = 2;
    public static final int EXCLUDE_SELF = 4;
    public static final int EXCLUDE_DESCENDANT = 8;

    public static Map<Integer, Integer> getNumMap(String s) {
        if (Utils.isNotBlank(s)) {
            Map<Integer, Integer> rsMap = new HashMap<>();
            String[] nums = s.trim().split(NUM_SEP);
            String num;
            int flags;
            for (String n : nums) {
                num = n.trim();
                if (num.startsWith("+")) {
                    flags = (INCLUDE_SELF | INCLUDE_DESCENDANT);
                    num = num.substring(1);
                } else if (num.startsWith("-")) {
                    flags = (EXCLUDE_SELF | EXCLUDE_DESCENDANT);
                    num = num.substring(1);
                } else if (num.startsWith("^")) {
                    flags = EXCLUDE_SELF;
                    num = num.substring(1);
                } else if (num.endsWith("+")) {
                    flags = INCLUDE_DESCENDANT;
                    num = num.substring(0, num.length() - 1);
                } else if (num.endsWith("-")) {
                    flags = EXCLUDE_DESCENDANT;
                    num = num.substring(0, num.length() - 1);
                } else {
                    flags = INCLUDE_SELF;
                }
                if (Utils.isNotBlank(num))
                    rsMap.put(
                            Utils.parseInt(num, "Number"),
                            flags
                    );
                else
                    throw new RuntimeException("Invalid num: " + n);
            }
            return rsMap;
        }
        return null;
    }

    public static boolean isIncludeSelf(int v) {
        return has(v, INCLUDE_SELF);
    }

    public static boolean isIncludeDescendant(int v) {
        return has(v, INCLUDE_DESCENDANT);
    }

    public static boolean isExcludeSelf(int v) {
        return has(v, EXCLUDE_SELF);
    }

    public static boolean isExcludeDescendant(int v) {
        return has(v, EXCLUDE_DESCENDANT);
    }

    private static boolean has(int v, int flag) {
        return (v & flag) != 0;
    }


}
