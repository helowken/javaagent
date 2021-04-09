package agent.launcher.client;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

class AddressOptConfigs {
    private static final String KEY_ADDRESS = "ADDRESS";
    private static final String KEY_ENV_ADDR = "JA_ADDR";
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
        String addr = opts.get(KEY_ADDRESS);
        if (Utils.isBlank(addr))
            addr = System.getenv(KEY_ENV_ADDR);
        return addr;
    }
}
