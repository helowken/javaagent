package agent.common.config;

import java.util.regex.Pattern;

import static agent.base.utils.AssertUtils.assertTrue;

class TidUtils {
    private static final String patternString = "[a-zA-Z_][a-zA-Z0-9_]*";
    private static final Pattern idPattern = Pattern.compile(patternString);

    static void validate(String tid) {
        assertTrue(idPattern.matcher(tid).matches(), "Transformer id '" + tid + "' must match: " + patternString);
    }
}
