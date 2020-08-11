package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.args.parse.OptParser;
import agent.common.args.parse.specific.ChainFilterOptConfigs;
import agent.common.args.parse.specific.FilterOptConfigs;

import java.util.Collections;
import java.util.List;

public abstract class AbstractModuleParamParser<P extends ModuleParams> extends AbstractCmdParamParser<P> {
    @Override
    protected List<OptParser> getMoreParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        ChainFilterOptConfigs.getSuite()
                )
        );
    }
}
