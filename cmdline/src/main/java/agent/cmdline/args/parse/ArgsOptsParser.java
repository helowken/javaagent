package agent.cmdline.args.parse;

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
            if (!parseOpt(arg, argList, opts))
                params.add(arg);
        }
        return new ArgsOpts(
                opts,
                params.toArray(new String[0])
        );
    }

    private boolean parseOpt(String arg, ArgList argList, Opts opts) {
        for (OptParser optParser : optParserList) {
            if (optParser.parse(arg, argList, opts))
                return true;
        }
        return false;
    }

    public List<OptConfig> getOptConfigList() {
        List<OptConfig> rsList = new ArrayList<>();
        optParserList.forEach(
                optParser -> rsList.addAll(
                        optParser.getOptConfigList()
                )
        );
        return rsList;
    }
}
