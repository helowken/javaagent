package agent.cmdline.args.parse;

import java.util.List;

public interface OptParser {
    boolean parse(String arg, ArgList argList, Opts opts);

    List<OptConfig> getOptConfigList();
}
