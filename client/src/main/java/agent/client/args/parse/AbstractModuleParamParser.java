package agent.client.args.parse;

import agent.base.args.parse.*;
import agent.common.args.parse.specific.ChainFilterOptConfigs;
import agent.common.args.parse.specific.FilterOptConfigs;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractModuleParamParser<P extends ModuleParams> extends AbstractCmdParamParser<P> {

    @Override
    protected List<OptParser> getMoreParsers() {
        return Arrays.asList(
                new BooleanOptParser(
                        CommonOptConfigs.helpOpt
                ),
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        ChainFilterOptConfigs.getSuite()
                )
        );
    }
}
