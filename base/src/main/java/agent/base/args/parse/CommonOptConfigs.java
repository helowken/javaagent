package agent.base.args.parse;

public class CommonOptConfigs {
    private static final String KEY_HELP = "HELP";
    private static final String KEY_VERSION = "VERSION";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-h", "--help", KEY_HELP, "Help."),
            new OptConfig("-V", "--version", KEY_VERSION, "Output the version number.")
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static boolean isHelp(Opts opts) {
        return opts.getNotNull(KEY_HELP, false);
    }

    public static boolean isVersion(Opts opts) {
        return opts.getNotNull(KEY_VERSION, false);
    }
}
