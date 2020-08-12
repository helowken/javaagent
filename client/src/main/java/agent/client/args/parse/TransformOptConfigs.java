package agent.client.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class TransformOptConfigs {
    private static final String KEY_TRANSFORMER_ID = "TRANSFORMER_ID";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-t",
                    "--transformer-id",
                    KEY_TRANSFORMER_ID,
                    "Desc TODO."
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getTransformId(Opts opts) {
        return opts.get(KEY_TRANSFORMER_ID);
    }
}
