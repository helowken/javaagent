package agent.cmdline.args.parse;

import java.util.List;

public interface CmdParamParser<P> {
    P parse(String[] args);

    List<OptConfig> getOptConfigList();

    default boolean hasOptConfig() {
        return !getOptConfigList().isEmpty();
    }
}
