package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.OptParser;

import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;

public class DefaultParamParser extends AbstractCmdParamParser<CmdParams> {
    public static final DefaultParamParser DEFAULT = new DefaultParamParser(
            Collections.singletonList(
                    getHelpOptParser()
            )
    );
    private final List<OptParser> optParsers;

    public DefaultParamParser(List<OptParser> optParsers) {
        this.optParsers = Collections.unmodifiableList(optParsers);
    }

    @Override
    protected List<OptParser> getOptParsers() {
        return optParsers;
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}