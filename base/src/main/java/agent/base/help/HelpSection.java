package agent.base.help;

import agent.base.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class HelpSection extends AbstractHelpInfo {
    public static final String PADDING_1 = "  ";
    public static final String PADDING_2 = "    ";
    private final String header;
    private final String padding;
    private final List<HelpInfo> children = new ArrayList<>();

    public HelpSection(String header, String padding) {
        this.header = header;
        this.padding = padding;
    }

    public HelpSection add(HelpInfo c) {
        if (c == null)
            throw new IllegalArgumentException();
        children.add(c);
        if (c instanceof AbstractHelpInfo) {
            ((AbstractHelpInfo) c).parent = this;
        }
        return this;
    }

    public HelpSection add(Collection<HelpInfo> cs) {
        cs.forEach(this::add);
        return this;
    }

    public HelpSection invoke(Consumer<HelpSection> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public void print(StringBuilder sb) {
        if (Utils.isNotBlank(header)) {
            int idx = sb.length() - 1;
            if (!(idx > 1 &&
                    sb.charAt(idx) == '\n' &&
                    sb.charAt(idx - 1) == '\n'))
                sb.append("\n");

            sb.append(
                    getParentPadding()
            ).append(header)
                    .append("\n\n");
        }

        children.forEach(
                child -> child.print(sb)
        );
    }

    @Override
    String getPadding() {
        return padding;
    }
}
