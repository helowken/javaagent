package agent.cmdline.args.parse;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCmdParamParser<P> implements CmdParamParser<P> {
    private ArgsOptsParser argsOptsParser;

    protected abstract List<OptParser> getOptParsers();

    protected abstract P convert(ArgsOpts argsOpts);

    @Override
    public List<OptConfig> getOptConfigList() {
        return getArgsOptsParser().getOptConfigList();
    }

    protected void preParse(String[] args) {
    }

    @Override
    public P parse(String[] args) {
        preParse(args);
        return convert(
                getArgsOptsParser().parse(args)
        );
    }

    protected OptParser getUnknownOptParser() {
        return UnknownOptParser.getInstance();
    }

    private synchronized ArgsOptsParser getArgsOptsParser() {
        if (argsOptsParser == null) {
            List<OptParser> optParsers = new ArrayList<>();

            List<OptParser> moreParsers = getOptParsers();
            if (moreParsers != null)
                optParsers.addAll(moreParsers);

            OptParser unknownOptParser = getUnknownOptParser();
            if (unknownOptParser != null)
                optParsers.add(unknownOptParser);

            argsOptsParser = new ArgsOptsParser(optParsers);
        }
        return argsOptsParser;
    }
}


