package agent.launcher.client;

import agent.base.utils.HostAndPort;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.OptValueType;
import agent.cmdline.args.parse.Opts;

class ClientLauncherOptConfigs {
    private static final String KEY_HOST = "HOST";
    private static final String KEY_PORT = "PORT";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 10100;
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
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static HostAndPort getHostAndPort(Opts opts) {
        return new HostAndPort(
                opts.getNotNull(KEY_HOST, DEFAULT_HOST),
                opts.getNotNull(KEY_PORT, DEFAULT_PORT)
        );
    }
}
