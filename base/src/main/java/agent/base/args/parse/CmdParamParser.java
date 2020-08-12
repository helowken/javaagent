package agent.base.args.parse;

import java.util.List;

public interface CmdParamParser<P> {
    P parse(String[] args);

    List<OptConfig> getOptConfigList();
}
