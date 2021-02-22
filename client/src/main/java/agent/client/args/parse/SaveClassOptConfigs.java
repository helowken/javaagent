package agent.client.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class SaveClassOptConfigs {
    private static final String KEY_WITH_SUB_CLASSES = "WITH_SUB_CLASSES";
    private static final String KEY_WITH_SUB_TYPES = "WITH_SUB_TYPES";

    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-sc",
                    "--with-sub-classes",
                    KEY_WITH_SUB_CLASSES,
                    "With sub classes which superclass is the target class."
            ),
            new OptConfig(
                    "-st",
                    "--with-sub-types",
                    KEY_WITH_SUB_TYPES,
                    "With sub types which can be present by the target class."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static boolean isWithSubClasses(Opts opts) {
        return opts.getNotNull(KEY_WITH_SUB_CLASSES, false);
    }

    public static boolean isWithSubTypes(Opts opts) {
        return opts.getNotNull(KEY_WITH_SUB_TYPES, false);
    }
}
