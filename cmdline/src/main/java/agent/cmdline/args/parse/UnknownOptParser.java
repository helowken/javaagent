package agent.cmdline.args.parse;

import java.util.Collections;
import java.util.List;

public class UnknownOptParser implements OptParser {
    private static final UnknownOptParser instance = new UnknownOptParser();

    public static UnknownOptParser getInstance() {
        return instance;
    }

    private UnknownOptParser() {
    }

    @Override
    public boolean parse(String arg, ArgList argList, Opts opts) {
        if (OptConfig.isOpt(arg))
            throw new RuntimeException("Unknown option: " + arg);
        return false;
    }

    @Override
    public List<OptConfig> getOptConfigList() {
        return Collections.emptyList();
    }
}
