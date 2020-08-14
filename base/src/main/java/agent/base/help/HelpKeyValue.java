package agent.base.help;

import agent.base.utils.IndentUtils;
import agent.base.utils.Utils;

public class HelpKeyValue extends AbstractHelpInfo {
    private static final String DESC_INDENT = IndentUtils.getIndent(7);
    private static final int DESC_INDENT_LENGTH = DESC_INDENT.length();
    private final String key;
    private final String value;

    public HelpKeyValue(String key, String value) {
        if (Utils.isBlank(key))
            throw new IllegalArgumentException();
        this.key = key;
        this.value = value;
    }

    @Override
    public void print(StringBuilder sb) {
        String totalPadding = getTotalPadding();
        sb.append(totalPadding).append(key);
        if (Utils.isNotBlank(value)) {
            int restLength = DESC_INDENT_LENGTH - key.length() - totalPadding.length();
            if (restLength >= 0)
                printSpaces(sb, restLength);
            else
                sb.append('\n').append(totalPadding).append(DESC_INDENT);
            sb.append(value);
        }
        sb.append('\n');
    }
}
