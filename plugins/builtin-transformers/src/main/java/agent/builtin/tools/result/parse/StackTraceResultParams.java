package agent.builtin.tools.result.parse;

import agent.base.args.parse.ArgsOpts;

import java.util.Map;

public class StackTraceResultParams extends AbstractResultParams {
    private Map<Integer, String> metadata;

    public StackTraceResultParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public Map<Integer, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<Integer, String> metadata) {
        this.metadata = metadata;
    }
}
