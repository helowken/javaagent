package agent.base.args.parse;

public interface OptParser {
    boolean parse(String arg, ArgList argList, Opts opts);
}
