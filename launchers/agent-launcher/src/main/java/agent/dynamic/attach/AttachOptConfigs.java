package agent.dynamic.attach;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

class AttachOptConfigs {
    private static String KEY_LEGACY = "LEGACY";
    private static String KEY_VERBOSE = "VERBOSE";

    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-l",
                    "--legacy",
                    KEY_LEGACY,
                    "Use tools.jar loading agent to java process."
            ),
            new OptConfig(
                    "-v",
                    "--verbose",
                    KEY_VERBOSE,
                    "Display attach details."
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static boolean isLegacy(Opts opts) {
        return opts.getNotNull(KEY_LEGACY, false);
    }

    static boolean isVerbose(Opts opts) {
        return opts.getNotNull(KEY_VERBOSE, false);
    }
}