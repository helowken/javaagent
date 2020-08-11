package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.args.parse.OptParser;

import java.util.ArrayList;
import java.util.List;

public class TransformParamParser extends AbstractModuleParamParser<TransformParams> {
    @Override
    protected List<OptParser> getMoreParsers() {
        List<OptParser> parentOptParsers = super.getMoreParsers();
        List<OptParser> optParsers = new ArrayList<>();
        if (parentOptParsers != null)
            optParsers.addAll(parentOptParsers);
        optParsers.add(
                new KeyValueOptParser(
                        TransformOptConfigs.getSuite()
                )
        );
        return optParsers;
    }

    @Override
    protected TransformParams convert(ArgsOpts argsOpts) {
        return new TransformParams(argsOpts);
    }
}
