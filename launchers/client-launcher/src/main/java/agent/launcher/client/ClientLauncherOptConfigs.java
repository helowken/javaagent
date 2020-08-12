package agent.launcher.client;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;
import agent.base.utils.HostAndPort;

class ClientLauncherOptConfigs {
    private static final String KEY_HOST = "HOST";
    private static final String KEY_PORT = "PORT";
    private static final String KEY_RUNNER_TYPE = "RUNNER_TYPE";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_RUNNER_TYPE = "clientCmdRunner";
    private static final int DEFAULT_PORT = 10086;
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    null,
                    "--host",
                    KEY_HOST,
                    "Specify server address. Default: \"" + DEFAULT_HOST + "\""
            ),
            new OptConfig(
                    null,
                    "--port",
                    KEY_PORT,
                    "Specify server port. Default: " + DEFAULT_PORT,
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    null,
                    "--runner-type",
                    KEY_RUNNER_TYPE,
                    "Specify client runner type. Default: \"" + DEFAULT_RUNNER_TYPE + "\""
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static HostAndPort getHostAndPort(Opts opts) {
        return new HostAndPort(
                opts.getNotNull(KEY_HOST, DEFAULT_HOST),
                opts.getNotNull(KEY_HOST, DEFAULT_PORT)
        );
    }

    static String getRunnerType(Opts opts) {
        return opts.getNotNull(KEY_RUNNER_TYPE, DEFAULT_RUNNER_TYPE);
    }
}
