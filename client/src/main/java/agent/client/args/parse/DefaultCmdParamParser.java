package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.OptParser;
import agent.base.utils.Utils;

import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;

public class DefaultCmdParamParser<P extends CmdParams> extends AbstractCmdParamParser<P> {
    private final Class<P> clazz;

    public DefaultCmdParamParser(Class<P> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected List<OptParser> getOptParsers() {
        return Collections.singletonList(
                getHelpOptParser()
        );
    }

    @Override
    protected P convert(ArgsOpts argsOpts) {
        return Utils.wrapToRtError(
                () -> clazz.getConstructor(ArgsOpts.class).newInstance(argsOpts)
        );
    }
}
