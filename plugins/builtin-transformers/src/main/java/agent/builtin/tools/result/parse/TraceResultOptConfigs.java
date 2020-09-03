package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;

class TraceResultOptConfigs {
    private static final String KEY_OUTPUT = "OUTPUT";
    private static final String KEY_CONTENT_SIZE = "CONTENT_SIZE";
    private static final int DEFAULT_CONTENT_SIZE = 100;
    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT,
                    "Desc TODO."
            ),
            new OptConfig(
                    "-s",
                    "--content-size",
                    KEY_CONTENT_SIZE,
                    "Desc TODO.",
                    OptValueType.INT,
                    false
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static int getContentSize(Opts opts) {
        return opts.getNotNull(KEY_CONTENT_SIZE, DEFAULT_CONTENT_SIZE);
    }

    static String getOutput(Opts opts) {
        return opts.get(KEY_OUTPUT);
    }
}
