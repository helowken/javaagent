package agent.launcher.client;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

class AddressOptConfigs {
    private static final String KEY_ADDRESS = "ADDRESS";
    static final String OPT_ADDR = "-a";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    OPT_ADDR,
                    "--addr",
                    KEY_ADDRESS,
                    "Specify server address.\n" +
                            "Multiple addresses are separated by ','.\n" +
                            "Default: \"" + AddressUtils.defaultAddress + "\""
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    static String getAddress(Opts opts) {
        return opts.get(KEY_ADDRESS);
    }
}
