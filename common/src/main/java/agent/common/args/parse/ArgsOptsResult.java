package agent.common.args.parse;

import java.util.List;

public class ArgsOptsResult {
    private final Opts opts;
    private final String[] args;

    ArgsOptsResult(Opts opts, String[] args) {
        this.opts = opts;
        this.args = args;
    }

    public int argSize() {
        return args.length;
    }

    public String[] getArgs() {
        return args;
    }

    public String getArg(int idx) {
        if (idx >= args.length)
            throw new IllegalArgumentException("Invalid arg index: " + idx);
        return args[idx];
    }

    public int optSize() {
        return opts.size();
    }

    public int sizeOfOpt(String key) {
        return opts.sizeOf(key);
    }

    public <T> T getOpt(String key) {
        return opts.get(key);
    }

    public List<Object> getOpts(String key) {
        return opts.getList(key);
    }

}
