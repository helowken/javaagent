package agent.cmdline.help;

import agent.base.utils.IndentUtils;
import agent.base.utils.Utils;

public class HelpKeyValue extends AbstractHelpInfo {
    private static final String DESC_INDENT = IndentUtils.getIndent(7);
    private static final int DESC_INDENT_LENGTH = DESC_INDENT.length();
    private static final int GAP_LENGTH = 4;
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
            String[] rows = Utils.splitToArray(value, "\n");
            int restLength = DESC_INDENT_LENGTH - key.length() - totalPadding.length() - GAP_LENGTH;
            if (restLength >= 0)
                printSpaces(sb, restLength + GAP_LENGTH);
            else
                sb.append('\n').append(DESC_INDENT);
            sb.append(rows[0]);

            for (int i = 1; i < rows.length; ++i) {
                sb.append('\n').append(DESC_INDENT).append(rows[i]);
            }
        }
        sb.append('\n');
    }
}
