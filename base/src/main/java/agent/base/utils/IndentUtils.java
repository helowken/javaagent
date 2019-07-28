package agent.base.utils;

import java.util.HashMap;
import java.util.Map;

public class IndentUtils {
    private static final String INDENT = "    ";
    private static final Map<Integer, String> levelToIndent = new HashMap<>();

    static {
        for (int i = 0; i <= 10; ++i) {
            levelToIndent.put(i, getIndent(i, INDENT));
        }
    }

    public static String getIndent() {
        return INDENT;
    }

    public static String getIndent(int level) {
        return levelToIndent.putIfAbsent(level, getIndent(level, INDENT));
    }

    public static String getIndent(int level, String indent) {
        if (level <= 0)
            return "";
        return getIndent(level - 1) + indent;
    }
}
