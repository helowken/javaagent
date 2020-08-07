package agent.builtin.tools.result.parse;

import agent.common.args.parse.OptConfig;
import agent.common.args.parse.OptConfigSuite;
import agent.common.args.parse.OptionValueType;
import agent.common.args.parse.Opts;

class TraceResultOptConfig {
    private static final String KEY_OUTPUT = "OUTPUT";
    private static final String KEY_CONTENT_SIZE = "CONTENT_SIZE";
    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-o", "--output", KEY_OUTPUT),
            new OptConfig("-s", "--content-size", KEY_CONTENT_SIZE, OptionValueType.INT, false)
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static int getContentSize(Opts opts) {
        return opts.getNotNull(KEY_CONTENT_SIZE, 50);
    }

    static String getOutput(Opts opts) {
        return opts.get(KEY_OUTPUT);
    }
}
