package agent.builtin.tools.result.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

class CostTimeResultOptConfigs {
    private static final String KEY_RANGE = "COST_TIME_RANGE";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-r",
                    "--range",
                    KEY_RANGE,
                    "TODO."
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static String getRange(Opts opts) {
        return opts.get(KEY_RANGE);
    }
}
