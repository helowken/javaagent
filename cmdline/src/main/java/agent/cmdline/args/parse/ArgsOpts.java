package agent.cmdline.args.parse;

import agent.cmdline.exception.ArgMissingException;

import java.util.Arrays;
import java.util.List;

public class ArgsOpts {
    private final Opts opts;
    private final String[] args;

    public ArgsOpts(Opts opts, String[] args) {
        this.opts = opts;
        this.args = args;
    }

    public int argSize() {
        return args.length;
    }

    public String[] getArgs() {
        return args;
    }

    public String getArg(int idx, String argName) {
        if (idx >= args.length)
            throw new ArgMissingException(argName);
        return args[idx];
    }

    public int optSize() {
        return opts.size();
    }

    public int sizeOfOpt(String key) {
        return opts.sizeOf(key);
    }

    public Opts getOpts() {
        return opts;
    }

    public <T> T getOptValue(String key) {
        return opts.get(key);
    }

    public List<Object> getOptValues(String key) {
        return opts.getList(key);
    }

    @Override
    public String toString() {
        return "Args: " + Arrays.toString(args) + ", Opts: " + opts.toString();
    }
}
