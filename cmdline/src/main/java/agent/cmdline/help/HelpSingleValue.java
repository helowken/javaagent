package agent.cmdline.help;

import agent.base.utils.Utils;

public class HelpSingleValue extends AbstractHelpInfo {
    private final String value;

    public HelpSingleValue(String value) {
        if (Utils.isBlank(value))
            throw new IllegalArgumentException();
        this.value = value;
    }

    @Override
    public void print(StringBuilder sb) {
        sb.append(
                getTotalPadding()
        ).append(value).append('\n');
    }
}
