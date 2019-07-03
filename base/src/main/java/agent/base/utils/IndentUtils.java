package agent.base.utils;

public class IndentUtils {
    private static final String INDENT = "    ";

    public static String getIndent() {
        return INDENT;
    }

    public static String getIndent(int level) {
        return getIndent(level, INDENT);
    }

    public static String getIndent(int level, String indent) {
        if (level <= 0)
            return "";
        return getIndent(level - 1) + indent;
    }
}
