package agent.launcher.client;

import agent.base.parser.AbstractArgsCmdParser;
import agent.base.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ClientArgsCmdParser extends AbstractArgsCmdParser<ClientOptions, ClientParams> {
    private static final String RUNNER_TYPE_CMD = "clientCmdRunner";
    private static final String RUNNER_TYPE_FILE = "clientFileRunner";
    static final String RUNNER_TYPE_INTERACT = "clientInteractRunner";
    private static final Set<String> runnerTypes = new HashSet<>();

    private static final String OPT_HOST = "-h";
    private static final String OPT_PORT = "-p";
    private static final String OPT_RUNNER_TYPE = "-t";

    static {
        runnerTypes.add(RUNNER_TYPE_INTERACT);
        runnerTypes.add(RUNNER_TYPE_CMD);
        runnerTypes.add(RUNNER_TYPE_FILE);
    }

    private List<String> argList = new ArrayList<>();

    @Override
    protected ClientOptions createOptions() {
        return new ClientOptions();
    }

    @Override
    protected ClientParams createParams() {
        return new ClientParams();
    }

    @Override
    protected String getMsgFile() {
        return "client.txt";
    }

    @Override
    protected int parseBeforeOptions(ClientParams params, String[] args) throws Exception {
        int i = 0;
        params.configFilePath = getArg(args, i++, "configFile");
        return i;
    }

    @Override
    protected int parseOption(ClientOptions opts, String[] args, int startIdx) {
        int i = startIdx;
        switch (args[i]) {
            case OPT_HOST:
                opts.host = getArg(args, ++i, "host");
                break;
            case OPT_PORT:
                opts.port = Utils.parseInt(
                        getArg(args, ++i, "port"),
                        "port"
                );
                break;
            case OPT_RUNNER_TYPE:
                opts.runnerType = getArg(args, ++i, "runnerType");
                if (!runnerTypes.contains(opts.runnerType))
                    throw new RuntimeException("Invalid runner type: " + opts.runnerType);
                break;
            default:
                argList.add(args[i]);
                break;
        }
        return i;
    }

    @Override
    protected void parseAfterOptions(ClientParams params, String[] args, int startIdx) throws Exception {
        params.cmdArgs.add(params.opts.host);
        params.cmdArgs.add(params.opts.port);
        switch (params.opts.runnerType) {
            case RUNNER_TYPE_CMD:
                params.cmdArgs.addAll(argList);
                for (int i = startIdx; i < args.length; ++i) {
                    params.cmdArgs.add(args[i]);
                }
                break;
            case RUNNER_TYPE_FILE:
                throw new UnsupportedOperationException("Unsupported now.");
        }
    }
}

