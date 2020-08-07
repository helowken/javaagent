package agent.builtin.tools.result.parse;

import agent.common.args.parse.ArgsOpts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class CostTimeResultParams extends AbstractResultParams {
    private static final String RANGE_SEP = ",";
    private static final Set<Float> DEFAULT_RANGE = Collections.unmodifiableSet(
            new TreeSet<>(
                    Arrays.asList(0.9F, 0.95F, 0.99F)
            )
    );
    private final Set<Float> range;

    CostTimeResultParams(ArgsOpts argsOpts) {
        super(argsOpts);
        this.range = parseRates(
                CostTimeResultOptConfigs.getRange(argsOpts.getOpts())
        );
    }

    public Set<Float> getRange() {
        return range;
    }

    private Set<Float> parseRates(String s) {
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

    private Float parseRate(String t) {
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
