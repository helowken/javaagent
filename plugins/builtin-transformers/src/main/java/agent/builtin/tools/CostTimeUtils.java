package agent.builtin.tools;

import agent.builtin.tools.result.CostTimeResultHandler;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class CostTimeUtils {
    private static final String RATE_SEP = ",";
    private static final Set<Float> DEFAULT_RATES = new TreeSet<>(
            Arrays.asList(0.9F, 0.95F, 0.99F)
    );

    static void run(String[] args, CostTimeResultHandler resultBuilder) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: inputPath [skipAvgEq0] [rates]");
            System.exit(-1);
        }
        String inputPath = args[0];
        boolean skipAvgEq0 = args.length > 1 && args[1].equals("true");
        Set<Float> rates = parseRates(args.length > 2 ? args[2] : null);
        resultBuilder.printResult(inputPath, skipAvgEq0, rates);
    }

    private static Set<Float> parseRates(String s) {
        if (s == null)
            return DEFAULT_RATES;
        s = s.trim();
        if (s.isEmpty())
            return DEFAULT_RATES;
        String[] ts = s.split(RATE_SEP);
        Set<Float> rates = new TreeSet<>();
        for (String t : ts) {
            t = t.trim();
            try {
                rates.add(Float.parseFloat(t));
            } catch (NumberFormatException e) {
                System.err.println("Invalid rate: " + t);
                System.exit(1);
            }
        }
        return rates;
    }
}
