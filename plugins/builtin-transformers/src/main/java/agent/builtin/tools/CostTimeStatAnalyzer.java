package agent.builtin.tools;

import agent.builtin.tools.result.ResultHandler;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class CostTimeStatAnalyzer {
    private static final String RATE_SEP = ",";
    private static final Set<Float> DEFAULT_RATES = new TreeSet<>(
            Arrays.asList(0.9F, 0.95F, 0.99F)
    );
    private final ResultHandler resultBuilder;

    CostTimeStatAnalyzer(ResultHandler resultBuilder) {
        this.resultBuilder = resultBuilder;
    }

    void run(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: outputPath [skipAvgEq0] [rates]");
            System.exit(-1);
        }
        String outputPath = args[0];
        boolean skipAvgEq0 = args.length > 1 && args[1].equals("true");
        Set<Float> rates = parseRates(args.length > 2 ? args[2] : null);
        resultBuilder.printResult(outputPath, skipAvgEq0, rates);
    }

    private Set<Float> parseRates(String s) {
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
