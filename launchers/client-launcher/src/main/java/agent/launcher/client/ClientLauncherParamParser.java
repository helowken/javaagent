package agent.launcher.client;

import agent.base.args.parse.*;

import java.util.Arrays;
import java.util.List;

public class ClientLauncherParamParser extends AbstractCmdParamParser<ClientLauncherParams> {
    private final StoreOtherArgsOptParser storeOtherArgsOptParser = new StoreOtherArgsOptParser();

    @Override
    protected List<OptParser> getMoreParsers() {
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
    protected ClientLauncherParams convert(ArgsOpts argsOpts) {
        return new ClientLauncherParams(argsOpts);
    }

    List<String> getRestArgs() {
        return storeOtherArgsOptParser.getArgs();
    }
}
