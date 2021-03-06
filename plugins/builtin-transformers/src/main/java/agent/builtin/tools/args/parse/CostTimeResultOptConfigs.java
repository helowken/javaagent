package agent.builtin.tools.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class CostTimeResultOptConfigs {
    private static final String KEY_RANGE = "COST_TIME_RANGE";
    private static final String KEY_INVOKE = "INVOKE";
    private static final String RANGE_SEP = ",";
    private static final Set<Float> DEFAULT_RANGE = Collections.unmodifiableSet(
            new TreeSet<>(
                    Arrays.asList(0.9F, 0.95F, 0.99F)
            )
    );
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-r",
                    "--range",
                    KEY_RANGE,
                    "TODO."
            )
    );
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-i",
                    "--invoke",
                    KEY_INVOKE,
                    "Use invocation as output format."
            )
    );

    public static OptConfigSuite getKvSuite() {
        return kvSuite;
    }

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static boolean isInvoke(Opts opts) {
        return opts.getNotNull(KEY_INVOKE, false);
    }

    public static Set<Float> getRange(Opts opts) {
        return parseRates(
                opts.get(KEY_RANGE)
        );
    }

    private static Set<Float> parseRates(String s) {
        if (s == null)
            return DEFAULT_RANGE;
        s = s.trim();
        if (s.isEmpty())
            return DEFAULT_RANGE;
        String[] ts = s.split(RANGE_SEP);
        Set<Float> rates = new TreeSet<>();
        for (String t : ts) {
            rates.add(
                    parseRate(t)
            );
        }
        if (rates.size() < 2)
            throw new RuntimeException("number of avgRatesRange must > 1.");
        return Collections.unmodifiableSet(rates);
    }

    private static Float parseRate(String t) {
        String s = t.trim();
        try {
            Float rate = Float.parseFloat(s);
            if (rate < 0)
                throw new RuntimeException("avgRate must > 0: " + s);
            return rate;
        } catch (NumberFormatException e) {
            throw new RuntimeException("avgRatesRange is invalid: " + s);
        }
    }
}
