package agent.base.help;

abstract class AbstractHelpInfo implements HelpInfo {
    AbstractHelpInfo parent;

    String getPadding() {
        return "";
    }

    String getTotalPadding() {
        return getPadding() + getParentPadding();
    }

    String getParentPadding() {
        return parent == null ? "" : parent.getTotalPadding();
    }

    static void printSpaces(StringBuilder sb, int size) {
        for (int i = 0; i < size; ++i) {
            sb.append(' ');
        }
    }
}
