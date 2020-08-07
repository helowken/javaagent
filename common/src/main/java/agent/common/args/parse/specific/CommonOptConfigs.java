package agent.common.args.parse.specific;

import agent.common.args.parse.OptConfig;
import agent.common.args.parse.OptConfigSuite;
import agent.common.args.parse.Opts;

public class CommonOptConfigs {
    private static final String KEY_HELP = "HELP";
    private static final String KEY_VERSION = "VERSION";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-h", "--help", KEY_HELP),
            new OptConfig("-v", "--version", KEY_VERSION)
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static boolean isHelp(Opts opts) {
        return opts.getNotNull(KEY_HELP, false);
    }

    public static boolean isVersion(Opts opts) {
        return opts.getNotNull(KEY_VERSION, false);
    }
}
