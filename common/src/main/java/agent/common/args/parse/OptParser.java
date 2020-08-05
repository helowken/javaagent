package agent.common.args.parse;

public interface OptParser {
    boolean parse(String arg, ArgList argList, Opts opts);
}
