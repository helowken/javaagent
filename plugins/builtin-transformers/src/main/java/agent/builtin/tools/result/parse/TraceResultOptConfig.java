package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;

class TraceResultOptConfig {
    private static final String KEY_OUTPUT = "OUTPUT";
    private static final String KEY_CONTENT_SIZE = "CONTENT_SIZE";
    private static OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-o", "--output", KEY_OUTPUT),
            new OptConfig("-s", "--content-size", KEY_CONTENT_SIZE, OptValueType.INT, false)
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
