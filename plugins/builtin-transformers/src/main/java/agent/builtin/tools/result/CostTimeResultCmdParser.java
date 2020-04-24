package agent.builtin.tools.result;

import java.util.Set;
import java.util.TreeSet;

import static agent.builtin.tools.result.CostTimeResultOptions.DEFAULT_RATES;


public class CostTimeResultCmdParser extends ResultCmdParser<CostTimeResultOptions, CostTimeResultParams> {
    private static final String OPT_RATES = "-r";
    private static final String RATE_SEP = ",";

    @Override
    protected CostTimeResultOptions createOptions() {
        return new CostTimeResultOptions();
    }

    @Override
    protected CostTimeResultParams createParams() {
        return new CostTimeResultParams();
    }

    @Override
    protected String getMsgFile() {
        return "costTimeResult.txt";
    }

    @Override
    protected int parseOption(CostTimeResultParams params, CostTimeResultOptions opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_RATES:
                opts.rates = parseRates(
                        getArg(args, ++i, "avgRatesRange")
                );
                break;
            default:
                return super.parseOption(params, opts, args, currIdx);
        }
        return i;
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
            rates.add(
                    parseRate(t)
            );
        }
        if (rates.size() < 2)
            throw new RuntimeException("number of avgRatesRange must > 1.");
        return rates;
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

