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
    public static final String INDENT_1 = getIndent(1);
    public static final String INDENT_2 = getIndent(2);
    public static final String INDENT_3 = getIndent(3);
    public static final String INDENT_4 = getIndent(4);


    public static String getIndent() {
        return INDENT;
    }

    public static String getIndent(int level) {
        return levelToIndent.computeIfAbsent(level, key -> getIndent(level, INDENT));
    }

    public static String getIndent(int level, String indent) {
        if (level <= 0)
            return "";
        return getIndent(level - 1) + indent;
    }

    public static void main(String[] args) {
        System.out.println(INDENT_1);
        System.out.println(INDENT_2);
        System.out.println(INDENT_3);
        System.out.println(INDENT_4);
    }
}
