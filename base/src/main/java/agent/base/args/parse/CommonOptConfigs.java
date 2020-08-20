package agent.base.args.parse;

public class CommonOptConfigs {
    private static final String KEY_HELP = "HELP";
    private static final String KEY_VERSION = "VERSION";
    public static final OptConfig helpOpt = new OptConfig("-h", "--help", KEY_HELP, "Help.");
    private static final OptConfigSuite suite = new OptConfigSuite(
            helpOpt,
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

    public static String getHelpOptName() {
        return helpOpt.getName();
    }
}
