package agent.client.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class JavascriptExecOptConfigs {
    private static final String KEY_FILE = "FILE";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-f",
                    "--file",
                    KEY_FILE,
                    "Javascript file."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getFile(Opts opts) {
        return opts.get(KEY_FILE);
    }
}
