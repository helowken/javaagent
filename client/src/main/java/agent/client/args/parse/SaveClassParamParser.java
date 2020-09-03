package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.BooleanOptParser;
import agent.base.args.parse.OptParser;

import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;

public class SaveClassParamParser extends AbstractCmdParamParser<CmdParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return merge(
                getFilterOptParsers(),
                new BooleanOptParser(
                        SaveClassOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}
