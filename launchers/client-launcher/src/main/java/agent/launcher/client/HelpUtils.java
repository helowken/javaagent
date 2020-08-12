package agent.launcher.client;

import agent.base.args.parse.OptConfig;
import agent.base.utils.IndentUtils;
import agent.base.utils.Utils;

import java.util.List;

import static agent.base.utils.IndentUtils.INDENT_1;

class HelpUtils {
    private static final String DESC_INDENT = IndentUtils.getIndent(7);
    private static final int OPT_LENGTH = DESC_INDENT.length();
    private static final String OPT_NAME_SEP = ", ";

    static void printVersion() {
        System.out.println("JavaAgent 1.0.0");
    }

    static void printHelp(List<OptConfig> optConfigList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\n\n")
                .append(INDENT_1).append("ja [--GLOBAL-OPTIONS] <COMMAND> [--COMMAND-OPTIONS] [ARGUMENTS]\n\n");
        sb.append("Global Options:\n\n");
        optConfigList.forEach(
                optConfig -> printOpt(sb, optConfig)
        );
        sb.append("\n\n")
                .append("Type 'ja help <COMMAND>' to get command-specific help.");
        System.out.println(sb);
    }

    private static void printOpt(StringBuilder sb, OptConfig optConfig) {
        StrBuffer strBuf = new StrBuffer(sb);
        strBuf.append(INDENT_1);

        String fullName = optConfig.getFullName();
        boolean hasFullName = false;
        if (Utils.isNotBlank(fullName)) {
            strBuf.append(fullName);
            hasFullName = true;
        }

        String name = optConfig.getName();
        if (Utils.isNotBlank(name)) {
            if (hasFullName) {
                strBuf.append(OPT_NAME_SEP);
            }
            strBuf.append(name);
        }

        String desc = optConfig.getDesc();
        if (Utils.isNotBlank(desc)) {
            int restLen = OPT_LENGTH - strBuf.length;
            if (restLen > 0)
                printSpaces(sb, restLen);
            else
                sb.append("\n").append(DESC_INDENT);
            sb.append(desc);
        }
        sb.append("\n");
    }

    private static void printSpaces(StringBuilder sb, int size) {
        for (int i = 0; i < size; ++i) {
            sb.append(' ');
        }
    }

    private static class StrBuffer {
        final StringBuilder sb;
        int length = 0;

        private StrBuffer(StringBuilder sb) {
            this.sb = sb;
        }

        void append(String s) {
            sb.append(s);
            length += s.length();
        }
    }
}
