package agent.builtin.tools.result.parse;

import agent.base.args.parse.ArgsOpts;

public class StackTraceResultParams extends AbstractResultParams {
    private Object metadata;

    StackTraceResultParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public <T> T getMetadata() {
        return (T) metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}
