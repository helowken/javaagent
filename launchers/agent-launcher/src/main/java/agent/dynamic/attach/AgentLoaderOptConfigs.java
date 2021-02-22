package agent.dynamic.attach;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class AgentLoaderOptConfigs {
    private static final String KEY_CHANGE_CREDENTIAL = "CHANGE_CREDENTIAL";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-c",
                    "--change-credential",
                    KEY_CHANGE_CREDENTIAL,
                    "Change credential to target process."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static boolean isChangeCredential(Opts opts) {
        return opts.getNotNull(KEY_CHANGE_CREDENTIAL, false);
    }
}
