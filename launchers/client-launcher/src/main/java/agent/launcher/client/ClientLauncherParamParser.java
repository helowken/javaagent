package agent.launcher.client;

import agent.cmdline.args.parse.*;

import java.util.Arrays;
import java.util.List;

public class ClientLauncherParamParser extends AbstractCmdParamParser<CmdParams> {
    private final StoreOtherArgsOptParser storeOtherArgsOptParser = new StoreOtherArgsOptParser();

    @Override
    protected List<OptParser> getOptParsers() {
        return Arrays.asList(
                new BooleanOptParser(
                        CommonOptConfigs.getSuite()
                ),
                new KeyValueOptParser(
                        ClientLauncherOptConfigs.getSuite()
                ),
                storeOtherArgsOptParser
        );
    }

    @Override
    protected OptParser getUnknownOptParser() {
        return null;
    }

    @Override
    protected void preParse(String[] args) {
        storeOtherArgsOptParser.clear();
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }

    List<String> getRestArgs() {
        return storeOtherArgsOptParser.getArgs();
    }
}
