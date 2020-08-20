package agent.base.help;

import agent.base.args.parse.OptConfig;
import agent.base.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class HelpUtils {
    public static List<HelpInfo> convert(List<OptConfig> optConfigList) {
        return optConfigList.stream()
                .map(
                        optConfig -> new HelpKeyValue(
                                getOptConfigNames(optConfig),
                                optConfig.getDesc()
                        )
                )
                .collect(Collectors.toList());
    }

    private static String getOptConfigNames(OptConfig optConfig) {
        String s = optConfig.getFullName();
        String name = optConfig.getName();
        if (Utils.isBlank(s))
            s = name;
        else if (Utils.isNotBlank(name))
            s += ", " + name;
        return s;
    }

    public static String formatCmdString(String[] cmds) {
        if (cmds == null || cmds.length == 0)
            throw new IllegalArgumentException();
        if (cmds.length == 1)
            return cmds[0];
        StringBuilder sb = new StringBuilder();
        sb.append(cmds[0]).append(" (");
        for (int i = 1; i < cmds.length; ++i) {
            if (i > 1)
                sb.append(", ");
            sb.append(cmds[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
