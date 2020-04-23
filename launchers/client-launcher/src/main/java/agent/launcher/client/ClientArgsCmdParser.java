package agent.launcher.client;

import agent.base.parser.AbstractArgsCmdParser;
import agent.base.utils.Utils;

import java.util.Arrays;

class ClientArgsCmdParser extends AbstractArgsCmdParser<ClientOptions, ClientParams> {
    private static final String RUNNER_TYPE_CMD = "clientCmdRunner";
    private static final String RUNNER_TYPE_FILE = "clientFileRunner";
    static final String RUNNER_TYPE_CLIENT = "clientRunner";
    private static final String OPT_PORT = "-p";
    private static final String OPT_CMD = "-s";
    private static final String OPT_FILE = "-f";

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
            case OPT_PORT:
                opts.port = Utils.parseInt(
                        getArg(args, i++, "port"),
                        "port"
                );
                break;
            case OPT_CMD:
                opts.runnerType = RUNNER_TYPE_CMD;
                ++i;
                break;
            case OPT_FILE:
                opts.runnerType = RUNNER_TYPE_FILE;
                ++i;
                break;
            default:
                super.parseOption(opts, args, startIdx);
        }
        return i;
    }

    @Override
    protected void parseAfterOptions(ClientParams params, String[] args, int startIdx) throws Exception {
        params.args = Arrays.copyOfRange(args, startIdx, args.length);
        if (params.args.length == 0) {
            switch (params.opts.runnerType) {
                case RUNNER_TYPE_CMD:
                    throw newUsageError("Command not found.");
                case RUNNER_TYPE_FILE:
                    throw newUsageError("File not found.");
            }
        }
    }
}

