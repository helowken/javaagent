package agent.cmdline.args.parse;

class ArgList {
    private final String[] args;
    private int currIdx = 0;

    ArgList(String[] args) {
        if (args == null)
            throw new IllegalArgumentException();
        this.args = args;
    }

    boolean hasNext() {
        return currIdx < args.length;
    }

    String next() {
        return next("No more arguments.");
    }

    String next(String errMsg) {
        if (currIdx >= args.length)
            throw new RuntimeException(errMsg);
        return args[currIdx++];
    }

}
