package agent.common.args.parse;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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

    public String getArg(int idx) {
        return getArg(idx, () -> "Invalid arg index: " + idx);
    }

    public String getArg(int idx, Supplier<String> errMsgSupplier) {
        if (idx >= args.length)
            throw new IllegalArgumentException(
                    errMsgSupplier.get()
            );
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
