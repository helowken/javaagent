package agent.client.args.parse;

import agent.base.args.parse.*;
import agent.base.utils.Utils;

import java.util.Collections;
import java.util.List;

public class DefaultCmdParamParser<P extends CmdParams> extends AbstractCmdParamParser<P> {
    private final Class<P> clazz;

    public DefaultCmdParamParser(Class<P> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected List<OptParser> getMoreParsers() {
        return Collections.singletonList(
                new BooleanOptParser(
                        CommonOptConfigs.helpOpt
                )
        );
    }

    @Override
    protected P convert(ArgsOpts argsOpts) {
        return Utils.wrapToRtError(
                () -> clazz.getConstructor(ArgsOpts.class)
                        .newInstance(argsOpts)
        );
    }
}
