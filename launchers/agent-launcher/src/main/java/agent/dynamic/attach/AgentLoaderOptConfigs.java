package agent.dynamic.attach;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;

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
