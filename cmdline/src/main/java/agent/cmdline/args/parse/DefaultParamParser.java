package agent.cmdline.args.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultParamParser extends AbstractCmdParamParser<CmdParams> {
    public static final DefaultParamParser DEFAULT = addMore();
    private final List<OptParser> optParsers;

    public static DefaultParamParser addMore(Collection<OptParser> optParsers) {
        return addMore(
                optParsers.toArray(new OptParser[0])
        );
    }

    public static DefaultParamParser addMore(OptParser... optParsers) {
        List<OptParser> rsList = new ArrayList<>();
        rsList.add(
                new BooleanOptParser(
                        CommonOptConfigs.helpOpt
                )
        );
        if (optParsers != null && optParsers.length > 0)
            Collections.addAll(rsList, optParsers);
        return new DefaultParamParser(rsList);
    }

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
