package agent.common.args.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArgsOptsParser {
    private final List<OptParser> optParserList = new ArrayList<>();

    public ArgsOptsParser(Collection<OptParser> optParsers) {
        this(optParsers.toArray(new OptParser[0]));
    }

    public ArgsOptsParser(OptParser... optParsers) {
        if (optParsers == null || optParsers.length == 0)
            throw new IllegalArgumentException();
        Collections.addAll(optParserList, optParsers);
    }

    public ArgsOpts parse(String[] args) {
        ArgList argList = new ArgList(args);
        Opts opts = new Opts();
        List<String> params = new ArrayList<>();
        while (argList.hasNext()) {
            String arg = argList.next();
            if (OptConfig.isOpt(arg))
                parseOpt(arg, argList, opts);
            else
                params.add(arg);
        }
        return new ArgsOpts(
                opts,
                params.toArray(new String[0])
        );
    }

    private void parseOpt(String arg, ArgList argList, Opts opts) {
        for (OptParser optParser : optParserList) {
            if (optParser.parse(arg, argList, opts))
                return;
        }
        throw new RuntimeException("Unknown option: " + arg);
    }
}
