package agent.builtin.tools.result.parse;

import agent.common.args.parse.*;
import agent.common.args.parse.specific.CommonOptConfigs;
import agent.common.args.parse.specific.FilterOptConfigs;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractResultOptParser<P extends AbstractResultParams> implements ResultOptParser<P> {
    protected abstract List<OptParser> getMoreParsers();

    protected abstract P convert(ArgsOpts argsOpts);

    @Override
    public P parse(String[] args) {
        List<OptParser> optParsers = new ArrayList<>();
        optParsers.add(
                new BooleanOptParser(
                        CommonOptConfigs.getSuite()
                )
        );
        optParsers.add(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite()
                )
        );
        List<OptParser> moreParsers = getMoreParsers();
        if (moreParsers != null)
            optParsers.addAll(moreParsers);
        return convert(
                new ArgsOptsParser(optParsers).parse(args)
        );
    }


}


