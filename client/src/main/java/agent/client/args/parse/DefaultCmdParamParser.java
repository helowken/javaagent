package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.OptParser;

import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;

public class DefaultCmdParamParser extends AbstractCmdParamParser<CmdParams> {
    public static final DefaultCmdParamParser DEFAULT = new DefaultCmdParamParser();

    @Override
    protected List<OptParser> getOptParsers() {
        return Collections.singletonList(
                getHelpOptParser()
        );
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}
