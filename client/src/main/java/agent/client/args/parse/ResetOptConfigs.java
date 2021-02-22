package agent.client.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class ResetOptConfigs {
    private static final String KEY_PRUNE = "PRUNE";
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-p",
                    "--prune",
                    KEY_PRUNE,
                    "If remove all injected bytecode."
            )
    );

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static boolean isPrune(Opts opts) {
        return opts.getNotNull(KEY_PRUNE, false);
    }
}
