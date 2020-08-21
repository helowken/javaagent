package agent.client.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

class TransformOptConfigs {
    private static final String KEY_TRANSFORMER_ID = "TRANSFORMER_ID";
    private static final String DESC = "Specified an id of transformer which is used to do the transformation.\n" +
            "If no transformer is found by this id, a new transformer will be created with it.\n" +
            "Without this option, the default transformer is used to do the transformation.";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-ti",
                    "--transformer-id",
                    KEY_TRANSFORMER_ID,
                    DESC
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static String getTransformId(Opts opts) {
        return opts.get(KEY_TRANSFORMER_ID);
    }
}
