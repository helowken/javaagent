package agent.base.args.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreOtherArgsOptParser implements OptParser {
    private final List<String> args = new ArrayList<>();

    @Override
    public boolean parse(String arg, ArgList argList, Opts opts) {
        args.add(arg);
        return true;
    }

    @Override
    public List<OptConfig> getOptConfigList() {
        return Collections.emptyList();
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public void clear() {
        args.clear();
    }
}
